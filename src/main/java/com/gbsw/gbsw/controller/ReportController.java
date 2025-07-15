package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.ReportHandleRequest;
import com.gbsw.gbsw.dto.ReportSummary;
import com.gbsw.gbsw.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pending")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "신고 목록 조회", description = "PENDING 상태인 신고들을 게시글/댓글 단위로 묶어서 반환합니다.")
    public ResponseEntity<List<ReportSummary>> getPendingReports() {
        List<ReportSummary> summaries = reportService.getPendingReportSummaries();
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/handle")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "신고 처리", description = "게시글 또는 댓글 신고를 승인 또는 거절 처리합니다.")
    public ResponseEntity<String> handleReport(@RequestBody ReportHandleRequest request) {
        reportService.handleReport(request);
        return ResponseEntity.ok("신고 처리가 성공적으로 완료되었습니다.");
    }
}
