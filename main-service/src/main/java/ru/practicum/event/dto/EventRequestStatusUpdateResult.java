package ru.practicum.event.dto;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;

public class EventRequestStatusUpdateResult {
    Collection<ParticipationRequestDto> confirmedRequests;
    Collection<ParticipationRequestDto> rejectedRequests;
}
