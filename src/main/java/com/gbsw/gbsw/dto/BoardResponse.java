package com.gbsw.gbsw.dto;

import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.User;
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

    public static BoardResponse of(Board board, User user, long likeCount, boolean likedByUser) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getUsername())
                .createdAt(board.getCreatedAt())
                .likeCount(likeCount)
                .viewCount(board.getViewCount())
                .likedByUser(likedByUser)
                .build();
    }
}
