package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.model.enums.RequestStatus;

import java.util.Collection;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    Collection<Long> requestIds;
    RequestStatus status;
}
