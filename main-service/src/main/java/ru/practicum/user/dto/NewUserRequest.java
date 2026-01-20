package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserRequest {
    @NotNull
    @Email
    @Size(min = 6, max = 254, message = "Адрес почты должен содержать от 6 до 254 символов.")
    String email;

    @NotNull
    @Size(min = 2, max = 250, message = "Имя пользователя должно содержать от 2 до 250 символов.")
    String name;
}
