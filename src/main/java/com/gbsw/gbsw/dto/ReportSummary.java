package com.gbsw.gbsw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReportSummary {
    private Long contentId;         // 게시글 또는 댓글 ID
    private String contentType;     // "BOARD" 또는 "COMMENT"
    private List<String> reasons;   // 신고 사유 모음
}
