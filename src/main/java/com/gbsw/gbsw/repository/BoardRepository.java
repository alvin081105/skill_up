package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
