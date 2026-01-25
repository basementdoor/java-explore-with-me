package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUserId(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByIdAndUserId(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByIdAndUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByIdAndUserId(@PathVariable Long userId, @PathVariable Long eventId,
                                                 @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return eventService.updateEventByIdAndUserId(userId, eventId, updateRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationRequests(@PathVariable Long userId,
                                                                       @PathVariable Long eventId) {
        return eventService.getEventParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsStatus(@PathVariable Long userId,
                                                                    @PathVariable Long eventId,
                                                                    @Valid @RequestBody
                                                                    EventRequestStatusUpdateRequest updateRequest) {
        return eventService.updateEventRequestsStatus(userId, eventId, updateRequest);
    }
}
