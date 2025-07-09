package com.gbsw.gbsw.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private String content;
    private Long parentId; // null이면 일반 댓글, 값 있으면 대댓글
}
