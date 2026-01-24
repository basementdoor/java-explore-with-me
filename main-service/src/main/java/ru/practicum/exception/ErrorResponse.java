package ru.practicum.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String message;
    private String reason;
    private String status;
    private LocalDateTime timestamp;
}
