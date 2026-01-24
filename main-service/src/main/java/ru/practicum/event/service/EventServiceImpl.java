package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.RequestStatus;
import ru.practicum.RequestUpdateStatus;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.StatsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventSort;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, EventSort sort,
                                               int from, int size, HttpServletRequest httpServletRequest) {
        Pageable pageable = getPageable(sort, from, size);
        validateDatesRange(rangeStart, rangeEnd);
        Specification<Event> spec = Specification
                .where(EventSpecifications.isPublished())
                .and(EventSpecifications.textSearch(text))
                .and(EventSpecifications.inCategories(categories))
                .and(EventSpecifications.paid(paid))
                .and(EventSpecifications.eventDateBetween(rangeStart, rangeEnd))
                .and(EventSpecifications.onlyAvailable(onlyAvailable));

        statsClient.hit(httpServletRequest);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, Long> eventsViews = getEventsViews(events);

        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, eventsViews.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest servletRequest) {
        Event event = throwIfEventNotExist(eventId);
        statsClient.hit(servletRequest);
        long views = getEventsViews(List.of(event)).getOrDefault(eventId, 0L);
        return EventMapper.toEventFullDto(event, views);
    }

    @Override
    public List<EventFullDto> getFullEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        var pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("id"));
        validateDatesRange(rangeStart, rangeEnd);
        Specification<Event> spec = Specification
                .where(EventSpecifications.initiatedByUsers(users))
                .and(EventSpecifications.inStates(states))
                .and(EventSpecifications.inCategories(categories))
                .and(EventSpecifications.eventDateBetween(rangeStart, rangeEnd));

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, Long> eventsViews = getEventsViews(events);

        return events.stream()
                .map(event -> EventMapper.toEventFullDto(
                        event,
                        eventsViews.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventById(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = throwIfEventNotExist(eventId);

        validateEventStateUpdate(event, updateRequest);
        validateEventDateUpdate(event, updateRequest);
        EventMapper.updateToEvent(event, updateRequest);

        updateCategory(event, updateRequest.getCategory().getId());
        Event savedEvent = updateAdminEventStateAction(event, updateRequest.getStateAction());
        var views = getEventsViews(List.of(savedEvent)).getOrDefault(savedEvent.getId(), 0L);
        return EventMapper.toEventFullDto(savedEvent, views);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        throwIfUserNotExist(userId);
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("id"));
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        Map<Long, Long> eventsViews = getEventsViews(events);

        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, eventsViews.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = throwIfUserNotExist(userId);
        Category category = throwIfCategoryNotExist(newEventDto.getCategory());

        Event newEvent = EventMapper.toEvent(newEventDto);
        newEvent.setInitiator(user);
        newEvent.setCategory(category);
        return EventMapper.toEventFullDto(eventRepository.save(newEvent), 0);
    }

    @Override
    public EventFullDto getEventByIdAndUserId(Long userId, Long eventId) {
        throwIfUserNotExist(userId);
        Event event = throwIfEventByUserNotExist(eventId, userId);
        var eventViews = getEventsViews(List.of(event)).getOrDefault(eventId, 0L);
        return EventMapper.toEventFullDto(event, eventViews);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByIdAndUserId(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        throwIfUserNotExist(userId);
        Event event = throwIfEventByUserNotExist(eventId, userId);

        validateUserEventUpdate(event, updateRequest);
        EventMapper.updateToEvent(event, updateRequest);
        updateCategory(event, updateRequest.getCategory());
        updateUserEventStateAction(event, updateRequest.getStateAction());

        var eventViews = getEventsViews(List.of(event)).getOrDefault(eventId, 0L);
        return EventMapper.toEventFullDto(eventRepository.save(event), eventViews);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipationRequests(Long userId, Long eventId) {
        throwIfUserNotExist(userId);
        throwIfEventByUserNotExist(eventId, userId);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsStatus(Long userId, Long eventId,
                                                                    EventRequestStatusUpdateRequest updateRequest) {
        throwIfUserNotExist(userId);
        Event event = throwIfEventByUserNotExist(eventId, userId);
//        TODO: доделать после реализации request
        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());
        validateRequests(eventId, requests);
        return updateRequestsStatus(requests, event, updateRequest.getStatus());
    }

    private Event throwIfEventNotExist(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID: %s не найдено".formatted(eventId)));
    }

    private Event throwIfEventByUserNotExist(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Для пользователя не найдено событие с ID: " + eventId));
    }

    private User throwIfUserNotExist(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: %s не найден".formatted(id)));
    }

    private Category throwIfCategoryNotExist(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с ID: %s не найдена".formatted(categoryId)));
    }

    private void validateUserEventUpdate(Event event, UpdateEventUserRequest updateRequest) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии " +
                    "ожидания модерации");
        }
        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
    }

    private void validateEventStateUpdate(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getStateAction() == null) {
            return;
        }
        if (Objects.equals(updateRequest.getStateAction(), EventStateAction.REJECT_EVENT) &&
            Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано");
        }
        if (Objects.equals(updateRequest.getStateAction(), EventStateAction.PUBLISH_EVENT) &&
                Objects.equals(event.getState(), EventState.PENDING)) {
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
    }

    private void validateEventDateUpdate(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getEventDate() == null) {
            return;
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
    }

    private void validateDatesRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Некорректный диапазон дат: начало не может быть позднее даты окончания");
        }
    }

    private void validateRequests(Long eventId, List<ParticipationRequest> requests) {
        if (requests.stream().anyMatch(request -> request.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("Все запросы должны быть со статусом PENDING");
        }
        if (requests.stream().anyMatch(request -> !Objects.equals(request.getEvent().getId(), eventId))) {
            throw new ConflictException("Запрос не относится к событию.");
        }
    }

    private EventRequestStatusUpdateResult updateRequestsStatus(List<ParticipationRequest> requests, Event event,
                                                                RequestUpdateStatus status) {
        switch (status) {
            case CONFIRMED -> {
                return confirmRequests(event, requests, status);
            }
            case REJECTED -> {
                setRequestsStatuses(requests, status);
                return rejectRequests(requests);
            }
            default ->  throw new ValidationException("Неизвестный статус: " + status);
        }
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequest> requests,
                                                           RequestUpdateStatus status) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (event.getConfirmedRequests() + requests.size() > event.getParticipantLimit()) {
            throw new ConflictException("Уже достигнут лимит участников");
        }

        setRequestsStatuses(requests, status);

        event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        eventRepository.save(event);

        result.getConfirmedRequests().addAll(requests.stream().map(RequestMapper::toRequestDto).toList());
        return result;
    }

    private EventRequestStatusUpdateResult rejectRequests(List<ParticipationRequest> requests) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.getRejectedRequests().addAll(requests.stream().map(RequestMapper::toRequestDto).toList());
        return result;
    }

    private void setRequestsStatuses(List<ParticipationRequest> requests, RequestUpdateStatus status) {
        RequestStatus toSetStatus = status == RequestUpdateStatus.REJECTED
                ? RequestStatus.REJECTED
                : RequestStatus.CONFIRMED;
        requests.forEach(r -> r.setStatus(toSetStatus));
        requestRepository.saveAll(requests);
    }

    private void updateCategory(Event event, Long categoryId) {
        if (!Objects.equals(event.getCategory().getId(), categoryId)) {
            Category category = throwIfCategoryNotExist(categoryId);
            event.setCategory(category);
        }
    }

    private void updateUserEventStateAction(Event event, EventStateAction action) {
        if (action == null) {
            return;
        }
        if (Objects.equals(action, EventStateAction.SEND_TO_REVIEW)) {
            event.setState(EventState.PENDING);
        } else if (Objects.equals(action, EventStateAction.CANCEL_REVIEW)) {
            event.setState(EventState.CANCELED);
        }
    }

    private Event updateAdminEventStateAction(Event event, EventStateAction stateAction) {
        if (stateAction == null) {
            return event;
        }
        if (stateAction == EventStateAction.PUBLISH_EVENT) {
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (stateAction == EventStateAction.REJECT_EVENT) {
            event.setState(EventState.CANCELED);
        }
        return eventRepository.save(event);
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = LocalDateTime.now().minusYears(10);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        List<StatsDto> stats = statsClient.getStats(start, end, uris, true).getBody();
        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }

        return stats.stream()
                .collect(Collectors.toMap(
                        s -> extractEventId(s.getUri()),
                        StatsDto::getHits,
                        Long::sum
                ));
    }

    private Pageable getPageable(EventSort sort, int from, int size) {
        var pageNumber = from / size;
        return switch (sort) {
            case EVENT_DATE -> PageRequest.of(pageNumber, size, Sort.by("eventDate"));
            case VIEWS -> PageRequest.of(pageNumber, size, Sort.by("views"));
            case null -> PageRequest.of(pageNumber, size, Sort.by("id"));
        };
    }

    private Long extractEventId(String uri) {
        return Long.parseLong(uri.split("/")[2]);
    }
}
