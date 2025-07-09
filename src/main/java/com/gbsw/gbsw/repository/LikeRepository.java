package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Like;
import com.gbsw.gbsw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndBoard(User user, Board board);
    void deleteByUserAndBoard(User user, Board board);
    long countByBoard(Board board);
}
