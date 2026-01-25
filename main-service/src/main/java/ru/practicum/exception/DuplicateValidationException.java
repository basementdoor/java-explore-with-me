package ru.practicum.exception;

public class DuplicateValidationException extends RuntimeException {
    public DuplicateValidationException(String message) {
        super(message);
    }
}
