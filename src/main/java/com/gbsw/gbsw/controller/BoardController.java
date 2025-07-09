package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.BoardRequest;
import com.gbsw.gbsw.dto.BoardResponse;
import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Like;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.repository.BoardRepository;
import com.gbsw.gbsw.repository.LikeRepository;
import com.gbsw.gbsw.repository.UserRepository;
import com.gbsw.gbsw.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "전체 게시글 목록 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public List<BoardResponse> getAllBoards(HttpServletRequest requestObj) {
        User user = null;
        try {
            String token = jwtUtil.resolveToken(requestObj);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        User finalUser = user;
        return boardRepository.findAll().stream()
                .map(board -> convertToResponse(board, finalUser))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "단일 게시글 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public BoardResponse getBoard(@PathVariable Long id, HttpServletRequest requestObj) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);

        User user = null;
        try {
            String token = jwtUtil.resolveToken(requestObj);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        return convertToResponse(board, user);
    }

    @PostMapping
    @Operation(summary = "게시글 작성", security = @SecurityRequirement(name = "BearerAuth"))
    public BoardResponse createBoard(@RequestBody BoardRequest request, HttpServletRequest requestObj) {
        String token = jwtUtil.resolveToken(requestObj);
        String username = jwtUtil.validateAndGetUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .createdAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        return convertToResponse(boardRepository.save(board), user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", security = @SecurityRequirement(name = "BearerAuth"))
    public BoardResponse updateBoard(@PathVariable Long id, @RequestBody BoardRequest request, HttpServletRequest requestObj) {
        String token = jwtUtil.resolveToken(requestObj);
        String username = jwtUtil.validateAndGetUsername(token);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        return convertToResponse(boardRepository.save(board), board.getUser());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public String deleteBoard(@PathVariable Long id, HttpServletRequest requestObj) {
        String token = jwtUtil.resolveToken(requestObj);
        String username = jwtUtil.validateAndGetUsername(token);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
        return "삭제 완료";
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> toggleLike(@PathVariable Long id, HttpServletRequest req) {
        String username = jwtUtil.validateAndGetUsername(jwtUtil.resolveToken(req));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (likeRepository.existsByUserAndBoard(user, board)) {
            likeRepository.deleteByUserAndBoard(user, board);
            return ResponseEntity.ok("좋아요 취소됨");
        } else {
            Like like = Like.builder().user(user).board(board).build();
            likeRepository.save(like);
            return ResponseEntity.ok("좋아요 추가됨");
        }
    }

    private BoardResponse convertToResponse(Board board, User user) {
        long likeCount = likeRepository.countByBoard(board);
        boolean liked = user != null && likeRepository.existsByUserAndBoard(user, board);

        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getUsername())
                .createdAt(board.getCreatedAt())
                .likeCount(likeCount)
                .viewCount(board.getViewCount())
                .likedByUser(liked)
                .build();
    }
}
