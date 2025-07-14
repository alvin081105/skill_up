package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Comment;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.repository.BoardRepository;
import com.gbsw.gbsw.repository.CommentRepository;
import com.gbsw.gbsw.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 사용자 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/boards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "게시글 삭제 (관리자)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> deleteBoardByAdmin(@PathVariable Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setDeleted(true);
        boardRepository.save(board);
        return ResponseEntity.ok("게시글이 관리자에 의해 삭제되었습니다.");
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "댓글 삭제 (관리자)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> deleteCommentByAdmin(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (comment.getChildren().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.setDeleted(true);
            comment.setContent("삭제된 댓글입니다.");
            commentRepository.save(comment);
        }

        return ResponseEntity.ok("댓글이 관리자에 의해 삭제되었습니다.");
    }
}
