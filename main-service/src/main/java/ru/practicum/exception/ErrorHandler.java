package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Ошибка запроса",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Ошибка валидации аргументов: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Невалидные данные",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.error("Ошибка constraint validation: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Невалидные параметры запроса",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        log.warn("Сущность не найдена: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Необходимый объект не найден",
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DuplicateValidationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateValidationException e) {
        log.error("Дублирование данных: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Дупликация информации",
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        log.error("Конфликт данных: {}", e.getMessage());
        return buildResponse(
                e.getMessage(),
                "Конфликт данных",
                HttpStatus.CONFLICT
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            String message,
            String reason,
            HttpStatus status
    ) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setReason(reason);
        errorResponse.setStatus(status.name());
        errorResponse.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, status);
    }
}
