package com.gbsw.gbsw.dto;

import com.gbsw.gbsw.entity.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private boolean deleted;
    private long likeCount;
    private boolean likedByUser;
    private List<CommentResponse> children;

    public static CommentResponse from(Comment comment, long likeCount, boolean likedByUser) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .writer(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .deleted(comment.isDeleted())
                .likeCount(likeCount)
                .likedByUser(likedByUser)
                .children(comment.getChildren().stream()
                        .map(child -> CommentResponse.from(child, 0, false))
                        .collect(Collectors.toList()))
                .build();
    }
}
