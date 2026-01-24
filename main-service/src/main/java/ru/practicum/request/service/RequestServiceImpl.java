package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.RequestStatus;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        throwIfUserNotExist(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = throwIfUserNotExist(userId);
        Event event = throwIfEventNotExist(eventId);

        validateRequest(userId, event);

        RequestStatus status = getRequestStatus(event);
        ParticipationRequest request = RequestMapper.toRequest(event, user, status);
        updateEventConfirmedRequestsQuantity(request, event);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Для пользователя с ID: %s не найден запрос с ID: %s"
                        .formatted(userId, requestId)));
        request.setStatus(RequestStatus.CANCELED);
        updateEventConfirmedRequestsIfCanceled(request.getStatus(), request.getEvent());
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    private User throwIfUserNotExist(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: %s не найден".formatted(id)));
    }

    private Event throwIfEventNotExist(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID: %s не найдено".formatted(eventId)));
    }

    private void validateRequest(Long userId, Event event) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, event.getId())) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("нельзя добавить запрос на неопубликованное событие");
        }
    }

    private RequestStatus getRequestStatus(Event event) {
        boolean needModeration = event.getRequestModeration() != null && event.getRequestModeration();
        boolean hasLimit = event.getParticipantLimit() != null && event.getParticipantLimit() > 0;

        return (needModeration && hasLimit) ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
    }

    private void updateEventConfirmedRequestsQuantity(ParticipationRequest request, Event event) {
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
    }

    private void updateEventConfirmedRequestsIfCanceled(RequestStatus status, Event event) {
        if (status == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }
    }
}
