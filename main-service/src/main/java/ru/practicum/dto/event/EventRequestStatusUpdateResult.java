package ru.practicum.dto.event;

import ru.practicum.dto.participation.ParticipationRequestDto;

import java.util.Collection;

public class EventRequestStatusUpdateResult {
    Collection<ParticipationRequestDto> confirmedRequests;
    Collection<ParticipationRequestDto> rejectedRequests;
}
