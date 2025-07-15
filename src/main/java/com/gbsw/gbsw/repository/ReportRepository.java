package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Report;
import com.gbsw.gbsw.enums.ReportStatus;
import com.gbsw.gbsw.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // PENDING 상태인 모든 신고 조회
    List<Report> findByStatus(ReportStatus status);

    // 특정 게시글 또는 댓글에 대한 모든 신고 조회
    List<Report> findByBoard_IdAndStatus(Long boardId, ReportStatus status);
    List<Report> findByComment_IdAndStatus(Long commentId, ReportStatus status);

    // 특정 게시글 또는 댓글에 대한 모든 신고 전체 조회 (승인/거절 포함)
    List<Report> findByBoard_Id(Long boardId);
    List<Report> findByComment_Id(Long commentId);

    // 1시간 내 신고 횟수 조회
    List<Report> findByBoard_IdAndCreatedAtAfter(Long boardId, LocalDateTime time);
    List<Report> findByComment_IdAndCreatedAtAfter(Long commentId, LocalDateTime time);
}
