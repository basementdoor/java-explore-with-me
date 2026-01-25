package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.DuplicateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        int page = from / size;
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(PageRequest.of(page, size, Sort.by("id")))
                    .map(UserMapper::toUserDto)
                    .toList();
        }
        return userRepository.findAllById(ids).stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest userRequest) {
        Optional<User> userWithSameEmail = userRepository.findByEmail(userRequest.getEmail());
        if (userWithSameEmail.isPresent()) {
            throw new DuplicateValidationException("Данный email уже занят.");
        }
        User user = UserMapper.toUser(userRequest);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        throwIfUserNotExist(userId);
        userRepository.deleteById(userId);
    }

    private User throwIfUserNotExist(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: %s не найден".formatted(id)));
    }
}
