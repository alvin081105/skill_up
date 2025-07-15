package com.gbsw.gbsw.dto;

import lombok.Data;

@Data
public class ReportHandleRequest {
    private Long contentId;       // 게시글 또는 댓글 ID
    private String contentType;   // "BOARD" 또는 "COMMENT"
    private boolean approved;     // true: 승인(삭제), false: 거절
    private String adminComment;  // 관리자 코멘트 (필수)
}
