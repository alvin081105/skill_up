package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Comment;
import com.gbsw.gbsw.entity.CommentLike;
import com.gbsw.gbsw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByUserAndComment(User user, Comment comment);
    void deleteByUserAndComment(User user, Comment comment);
    long countByComment(Comment comment);
}
