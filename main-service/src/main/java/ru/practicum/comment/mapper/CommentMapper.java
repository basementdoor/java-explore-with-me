package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.enmus.CommentStatus;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .commentator(UserMapper.toUserShortDto(comment.getCommentator()))
                .eventId(comment.getEvent().getId())
                .status(comment.getStatus())
                .build();
    }

    public Comment toComment(NewCommentDto commentDto, User commentator, Event event) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .commentator(commentator)
                .event(event)
                .status(CommentStatus.PENDING)
                .build();
    }

    public List<CommentDto> toCommentDtos(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }
}
