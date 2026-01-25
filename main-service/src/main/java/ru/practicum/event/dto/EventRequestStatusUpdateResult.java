package ru.practicum.event.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateResult {
    Collection<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
    Collection<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}
