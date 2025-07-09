package com.gbsw.gbsw.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BoardResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private long likeCount;
    private int viewCount;
    private boolean likedByUser;
}
