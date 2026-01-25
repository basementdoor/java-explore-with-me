package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.RequestStatus;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class RequestMapper {

    public ParticipationRequestDto toRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequestor().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .event(request.getEvent().getId())
                .build();
    }

    public ParticipationRequest toRequest(Event event, User requester, RequestStatus status) {
        return ParticipationRequest.builder()
                .event(event)
                .requestor(requester)
                .status(status)
                .created(LocalDateTime.now())
                .build();
    }
}
