package com.gbsw.gbsw.dto;

import com.gbsw.gbsw.enums.ReportType;
import lombok.Getter;

@Getter
public class ReportRequest {
    private Long targetId; // boardId 또는 commentId
    private String reason;
    private ReportType reportType;
}
