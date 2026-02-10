package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.dto.UpdateCommentStatusRequest;
import ru.practicum.comment.enmus.CommentStatus;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    final CommentRepository commentRepository;
    final UserRepository userRepository;
    final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = throwIfUserNotExist(userId);
        Event event = throwIfEventNotExistAndPublished(eventId);

        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        throwIfUserNotExist(userId);

        var pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("created"));
        return CommentMapper.toCommentDtos(commentRepository.findAllByCommentatorId(userId, pageable));
    }

    @Override
    @Transactional
    public CommentDto updateUserComment(Long userId, Long commentId, UpdateCommentDto updateDto) {
        throwIfUserNotExist(userId);
        Comment comment = throwIfCommentByUserNotExist(commentId, userId);

        comment.setText(updateDto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteUserComment(Long userId, Long commentId) {
        throwIfUserNotExist(userId);
        throwIfCommentByUserNotExist(commentId, userId);

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size) {
        throwIfEventNotExistAndPublished(eventId);

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("created"));

        return CommentMapper.toCommentDtos(commentRepository.findAllByEventId(eventId, pageable));
    }

    @Override
    public List<CommentDto> getAdminComments(Long eventId, CommentStatus status, int from, int size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("created"));

        if (eventId != null && status != null) {
            throwIfEventNotExist(eventId);
            return CommentMapper.toCommentDtos(commentRepository.findAllByEventIdAndStatus(eventId, status, pageable));
        }

        if (eventId != null) {
            throwIfEventNotExist(eventId);
            return CommentMapper.toCommentDtos(commentRepository.findAllByEventId(eventId, pageable));
        }

        if (status != null) {
            return CommentMapper.toCommentDtos(commentRepository.findAllByStatus(status, pageable));
        }

        return CommentMapper.toCommentDtos(commentRepository.findAll(pageable).getContent());
    }

    @Override
    @Transactional
    public CommentDto updateCommentStatus(Long commentId, UpdateCommentStatusRequest updateRequest) {
        Comment comment = throwIfCommentNoExist(commentId);
        if (updateRequest.getStatus() == comment.getStatus()) {
            throw new ConflictException("Комментарий уже находится в статусе: " + updateRequest.getStatus());
        }

        comment.setStatus(updateRequest.getStatus());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Comment throwIfCommentNoExist(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий с ID: %s".formatted(commentId)));
    }

    private Event throwIfEventNotExist(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с ID: %s".formatted(eventId)));
    }

    private Event throwIfEventNotExistAndPublished(Long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с ID: %s не существует или не было опубликовано"
                        .formatted(eventId)));
    }

    private User throwIfUserNotExist(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: %s не найден".formatted(id)));
    }

    private Comment throwIfCommentByUserNotExist(Long commentId, Long userId) {
        return commentRepository.findByIdAndCommentatorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: %s не оставлял комментарий с ID: %s"
                        .formatted(userId, commentId)));
    }
}
