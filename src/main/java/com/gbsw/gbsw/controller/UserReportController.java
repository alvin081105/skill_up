package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.ReportRequest;
import com.gbsw.gbsw.service.ReportService;
import com.gbsw.gbsw.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth") // 사용자 인증 필요
public class UserReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고 생성", description = "게시글 또는 댓글을 신고합니다.")
    public ResponseEntity<String> createReport(
            @RequestBody ReportRequest request,
            @AuthenticationPrincipal User user
    ) {
        reportService.createReport(user, request);
        return ResponseEntity.ok("신고가 접수되었습니다.");
    }
}
