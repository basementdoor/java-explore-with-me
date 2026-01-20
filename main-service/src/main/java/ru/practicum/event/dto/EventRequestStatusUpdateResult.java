package ru.practicum.event.dto;

import ru.practicum.participation.dto.ParticipationRequestDto;

import java.util.Collection;

public class EventRequestStatusUpdateResult {
    Collection<ParticipationRequestDto> confirmedRequests;
    Collection<ParticipationRequestDto> rejectedRequests;
}
