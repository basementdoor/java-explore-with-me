package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentStatusRequest;
import ru.practicum.comment.enmus.CommentStatus;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/admin/comments")
public class AdminCommentController {

    final CommentService commentService;

    @GetMapping
    public List<CommentDto> getAdminComments(@RequestParam(required = false) Long eventId,
                                        @RequestParam(required = false) CommentStatus status,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return commentService.getAdminComments(eventId, status, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentStatus(@PathVariable Long commentId,
                                          @Valid @RequestBody UpdateCommentStatusRequest updateRequest) {
        return commentService.updateCommentStatus(commentId, updateRequest);
    }
}
