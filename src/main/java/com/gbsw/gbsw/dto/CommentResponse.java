package com.gbsw.gbsw.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private boolean deleted;

    private long likeCount;         // 총 좋아요 수
    private boolean likedByUser;    // 로그인 유저가 좋아요 눌렀는지 여부

    private List<CommentResponse> children; // 대댓글
}
