package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequest {
    Collection<Long> events;

    Boolean pinned;

    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов.")
    String title;
}
