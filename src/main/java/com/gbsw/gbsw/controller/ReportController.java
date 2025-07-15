package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.ReportRequest;
import com.gbsw.gbsw.dto.ReportResponse;
import com.gbsw.gbsw.entity.*;
import com.gbsw.gbsw.enums.ReportType;
import com.gbsw.gbsw.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Operation(
            summary = "신고 등록",
            description = "게시글 또는 댓글을 신고합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/{userId}")
    public ResponseEntity<String> report(@PathVariable Long userId, @RequestBody ReportRequest request) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("신고자 없음"));

        Report.ReportBuilder builder = Report.builder()
                .reporter(reporter)
                .reason(request.getReason())
                .reportType(request.getReportType());

        if (request.getReportType() == ReportType.BOARD) {
            Board board = boardRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
            builder.board(board);
        } else {
            Comment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
            builder.comment(comment);
        }

        reportRepository.save(builder.build());
        return ResponseEntity.ok("신고 완료");
    }

    @Operation(
            summary = "신고 전체 조회 (관리자 전용)",
            description = "모든 신고 내역을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/admin")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> result = reportRepository.findAll().stream().map(r -> ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .boardId(r.getBoard() != null ? r.getBoard().getId() : null)
                .commentId(r.getComment() != null ? r.getComment().getId() : null)
                .reason(r.getReason())
                .reportType(r.getReportType())
                .build()
        ).toList();

        return ResponseEntity.ok(result);
    }
}
