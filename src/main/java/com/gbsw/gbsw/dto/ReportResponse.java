package com.gbsw.gbsw.dto;

import com.gbsw.gbsw.enums.ReportType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private Long boardId;
    private Long commentId;
    private String reason;
    private ReportType reportType;
}
