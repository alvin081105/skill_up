package com.gbsw.gbsw.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private Long contentId;
    private String contentType; // "BOARD" 또는 "COMMENT"
    private String reason;
}
