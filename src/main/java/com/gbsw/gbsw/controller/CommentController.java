package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.CommentRequest;
import com.gbsw.gbsw.dto.CommentResponse;
import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Comment;
import com.gbsw.gbsw.entity.CommentLike;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.repository.BoardRepository;
import com.gbsw.gbsw.repository.CommentLikeRepository;
import com.gbsw.gbsw.repository.CommentRepository;
import com.gbsw.gbsw.repository.UserRepository;
import com.gbsw.gbsw.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/{boardId}")
    @Operation(summary = "댓글 등록", security = @SecurityRequirement(name = "BearerAuth"))
    public CommentResponse addComment(@PathVariable Long boardId,
                                      @RequestBody CommentRequest request,
                                      HttpServletRequest httpRequest) {
        String token = jwtUtil.resolveToken(httpRequest);
        String username = jwtUtil.validateAndGetUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Comment parent = null;
        if (request.getParentId() != null && request.getParentId() != 0) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .board(board)
                .parent(parent)
                .isDeleted(false)
                .build();

        Comment saved = commentRepository.save(comment);
        Comment loaded = commentRepository.findById(saved.getId()).orElseThrow();
        return toResponse(loaded, user);

    }

    @GetMapping("/{boardId}")
    @Operation(summary = "게시글의 댓글 전체 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public List<CommentResponse> getComments(@PathVariable Long boardId, HttpServletRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        User user = null;
        try {
            String token = jwtUtil.resolveToken(request);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        User finalUser = user;
        List<Comment> topLevelComments = commentRepository
                .findByBoardAndParentIsNullOrderByCreatedAtAsc(board);

        return topLevelComments.stream()
                .map(comment -> toResponse(comment, finalUser))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", security = @SecurityRequirement(name = "BearerAuth"))
    public CommentResponse updateComment(@PathVariable Long id,
                                         @RequestBody CommentRequest request,
                                         HttpServletRequest httpRequest) {
        String token = jwtUtil.resolveToken(httpRequest);
        String username = jwtUtil.validateAndGetUsername(token);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        comment.setContent(request.getContent());
        return toResponse(commentRepository.save(comment), comment.getUser());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        String username = jwtUtil.validateAndGetUsername(token);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(403).body("작성자만 삭제할 수 있습니다.");
        }

        if (comment.getChildren().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.setDeleted(true);
            comment.setContent("삭제된 댓글입니다.");
            commentRepository.save(comment);
        }

        return ResponseEntity.ok("삭제 처리 완료");
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "댓글 좋아요 토글", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        String username = jwtUtil.validateAndGetUsername(jwtUtil.resolveToken(request));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (commentLikeRepository.existsByUserAndComment(user, comment)) {
            commentLikeRepository.deleteByUserAndComment(user, comment);
            return ResponseEntity.ok("댓글 좋아요 취소됨");
        } else {
            CommentLike like = CommentLike.builder().user(user).comment(comment).build();
            commentLikeRepository.save(like);
            return ResponseEntity.ok("댓글 좋아요 추가됨");
        }
    }

    private CommentResponse toResponse(Comment comment, User user) {
        List<CommentResponse> children = comment.getChildren().stream()
                .map(c -> toResponse(c, user))
                .collect(Collectors.toList());

        long likeCount = commentLikeRepository.countByComment(comment);
        boolean liked = user != null && commentLikeRepository.existsByUserAndComment(user, comment);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .writer(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .deleted(comment.isDeleted())
                .likeCount(likeCount)
                .likedByUser(liked)
                .children(children)
                .build();
    }
}
