package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.dto.UpdateCommentStatusRequest;
import ru.practicum.comment.enmus.CommentStatus;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    CommentDto updateUserComment(Long userId, Long commentId, UpdateCommentDto updateDto);

    void deleteUserComment(Long userId, Long commentId);

    List<CommentDto> getEventComments(Long eventId, int from, int size);

    List<CommentDto> getAdminComments(Long eventId, CommentStatus status, int from, int size);

    CommentDto updateCommentStatus(Long commentId, UpdateCommentStatusRequest updateRequest);
}
