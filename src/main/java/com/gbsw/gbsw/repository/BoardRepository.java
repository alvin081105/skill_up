package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 최신순 (createdAt 내림차순)
    List<Board> findAllByIsDeletedFalseOrderByCreatedAtDesc();

    // 조회수 많은 순 (viewCount 내림차순)
    List<Board> findAllByIsDeletedFalseOrderByViewCountDesc();

    // 추천 많은 순 (Like 수 기준)
    @Query("SELECT b FROM Board b LEFT JOIN b.likes l WHERE b.isDeleted = false GROUP BY b ORDER BY COUNT(l) DESC")
    List<Board> findAllOrderByLikeCount();
}
