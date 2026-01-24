package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {

    public ParticipationRequestDto toRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequestor().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .event(request.getEvent().getId())
                .build();
    }

    public ParticipationRequest toRequest(ParticipationRequestDto requestDto) {
        return ParticipationRequest.builder()

                .build();
    }
}
