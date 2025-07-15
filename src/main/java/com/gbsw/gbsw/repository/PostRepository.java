package com.gbsw.gbsw.repository;

import com.gbsw.gbsw.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM posts WHERE MATCH(title, content) AGAINST (?1 IN BOOLEAN MODE)", nativeQuery = true)
    List<Post> searchByKeyword(String keyword);
}
