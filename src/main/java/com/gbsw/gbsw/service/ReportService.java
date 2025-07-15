package com.gbsw.gbsw.service;

import com.gbsw.gbsw.dto.ReportHandleRequest;
import com.gbsw.gbsw.dto.ReportRequest;
import com.gbsw.gbsw.dto.ReportSummary;
import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Comment;
import com.gbsw.gbsw.entity.Report;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.enums.ReportStatus;
import com.gbsw.gbsw.enums.ReportType;
import com.gbsw.gbsw.repository.BoardRepository;
import com.gbsw.gbsw.repository.CommentRepository;
import com.gbsw.gbsw.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /**
     * PENDING 상태인 신고들을 contentId + type 기준으로 그룹화하여 반환
     */
    @Transactional(readOnly = true)
    public List<ReportSummary> getPendingReportSummaries() {
        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);

        Map<String, Map<Long, List<String>>> grouped = new HashMap<>();
        for (Report report : pendingReports) {
            String type = report.getBoard() != null ? "BOARD" : "COMMENT";
            Long id = type.equals("BOARD") ? report.getBoard().getId() : report.getComment().getId();

            grouped
                    .computeIfAbsent(type, k -> new HashMap<>())
                    .computeIfAbsent(id, k -> new ArrayList<>())
                    .add(report.getReason());
        }

        List<ReportSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Map<Long, List<String>>> entry : grouped.entrySet()) {
            String type = entry.getKey();
            for (Map.Entry<Long, List<String>> item : entry.getValue().entrySet()) {
                summaries.add(new ReportSummary(item.getKey(), type, item.getValue()));
            }
        }

        return summaries;
    }

    /**
     * 관리자 신고 승인/거절 처리
     */
    @Transactional
    public void handleReport(ReportHandleRequest request) {
        if (request.getAdminComment() == null || request.getAdminComment().isBlank()) {
            throw new IllegalArgumentException("관리자 코멘트는 필수입니다.");
        }

        ReportStatus newStatus = request.isApproved() ? ReportStatus.RESOLVED : ReportStatus.REJECTED;

        if (request.getContentType().equals("BOARD")) {
            List<Report> reports = reportRepository.findByBoard_Id(request.getContentId());
            reports.forEach(r -> {
                r.setStatus(newStatus);
                r.setAdminComment(request.getAdminComment());
            });

            if (request.isApproved()) {
                Board board = boardRepository.findById(request.getContentId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
                board.softDelete();
            }

        } else if (request.getContentType().equals("COMMENT")) {
            List<Report> reports = reportRepository.findByComment_Id(request.getContentId());
            reports.forEach(r -> {
                r.setStatus(newStatus);
                r.setAdminComment(request.getAdminComment());
            });

            if (request.isApproved()) {
                Comment comment = commentRepository.findById(request.getContentId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
                comment.softDelete();
            }

        } else {
            throw new IllegalArgumentException("contentType은 BOARD 또는 COMMENT여야 합니다.");
        }
    }

    /**
     * 신고 생성 시 자동 삭제 체크 (1시간 내 5회 이상이면)
     */
    @Transactional
    public void checkAutoDelete(Report newReport) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Report> recentReports;

        if (newReport.getBoard() != null) {
            Long boardId = newReport.getBoard().getId();
            recentReports = reportRepository.findByBoard_IdAndCreatedAtAfter(boardId, oneHourAgo);

            if (recentReports.size() >= 5) {
                Board board = newReport.getBoard();
                board.softDelete();
                recentReports.forEach(r -> {
                    r.setStatus(ReportStatus.RESOLVED);
                    r.setAdminComment("시스템에 의해 삭제된 컨텐츠입니다.");
                });
            }

        } else if (newReport.getComment() != null) {
            Long commentId = newReport.getComment().getId();
            recentReports = reportRepository.findByComment_IdAndCreatedAtAfter(commentId, oneHourAgo);

            if (recentReports.size() >= 5) {
                Comment comment = newReport.getComment();
                comment.softDelete();
                recentReports.forEach(r -> {
                    r.setStatus(ReportStatus.RESOLVED);
                    r.setAdminComment("시스템에 의해 삭제된 컨텐츠입니다.");
                });
            }
        }
    }

    /**
     * 사용자 신고 생성
     */
    @Transactional
    public void createReport(User reporter, ReportRequest request) {
        Report report;

        if (request.getContentType().equals("BOARD")) {
            Board board = boardRepository.findById(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

            report = Report.builder()
                    .reporter(reporter)
                    .board(board)
                    .reportType(ReportType.BOARD)
                    .reason(request.getReason())
                    .build();

        } else if (request.getContentType().equals("COMMENT")) {
            Comment comment = commentRepository.findById(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

            report = Report.builder()
                    .reporter(reporter)
                    .comment(comment)
                    .reportType(ReportType.COMMENT)
                    .reason(request.getReason())
                    .build();

        } else {
            throw new IllegalArgumentException("contentType은 BOARD 또는 COMMENT여야 합니다.");
        }

        reportRepository.save(report);
        checkAutoDelete(report);
    }
}
