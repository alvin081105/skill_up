package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.ReportRequest;
import com.gbsw.gbsw.dto.ReportResponse;
import com.gbsw.gbsw.entity.*;
import com.gbsw.gbsw.enums.ReportType;
import com.gbsw.gbsw.repository.*;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/{userId}")
    public String report(@PathVariable Long userId, @RequestBody ReportRequest request) {
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
        return "신고 완료";
    }

    @GetMapping("/admin")
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream().map(r -> ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .boardId(r.getBoard() != null ? r.getBoard().getId() : null)
                .commentId(r.getComment() != null ? r.getComment().getId() : null)
                .reason(r.getReason())
                .reportType(r.getReportType())
                .build()
        ).toList();
    }
}
