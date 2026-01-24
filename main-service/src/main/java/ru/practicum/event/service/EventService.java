package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventSort;
import ru.practicum.event.enums.EventState;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getPublicEvents(String text, List<Long> categories,
                                        Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        EventSort sort, int from, int size,
                                        HttpServletRequest httpServletRequest);

    List<EventFullDto> getFullEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto getEventById(Long evenId, HttpServletRequest servletRequest);

    EventFullDto updateEventById(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getEventsByUserId(Long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByIdAndUserId(Long userId, Long eventId);

    EventFullDto updateEventByIdAndUserId(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<ParticipationRequestDto> getEventParticipationRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsStatus(Long userId, Long eventId,
                                                             EventRequestStatusUpdateRequest updateRequest);
}
