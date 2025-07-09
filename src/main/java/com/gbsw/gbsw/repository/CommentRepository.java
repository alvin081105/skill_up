package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardAndParentIsNullOrderByCreatedAtAsc(Board board); // 최상위 댓글만 조회
    List<Comment> findByParent(Comment parent); // 대댓글 조회
}
