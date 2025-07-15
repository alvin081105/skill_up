package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.BoardRequest;
import com.gbsw.gbsw.dto.BoardResponse;
import com.gbsw.gbsw.entity.Board;
import com.gbsw.gbsw.entity.Like;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.enums.SortType;
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
    @Operation(summary = "전체 게시글 목록 조회", security = {})
    public List<BoardResponse> getAllBoards(@RequestParam(name = "sortType", defaultValue = "LATEST") SortType sortType, HttpServletRequest request) {
        User user = null;
        try {
            String token = jwtUtil.resolveToken(request);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        List<Board> boards = switch (sortType) {
            case VIEWS -> boardRepository.findAllByIsDeletedFalseOrderByViewCountDesc();
            case LIKES -> boardRepository.findAllOrderByLikeCount();
            case LATEST -> boardRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc();
        };

        User finalUser = user;
        return boards.stream()
                .map(board -> BoardResponse.of(
                        board,
                        finalUser,
                        likeRepository.countByBoard(board),
                        finalUser != null && likeRepository.existsByUserAndBoard(finalUser, board)
                ))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "단일 게시글 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public BoardResponse getBoard(@PathVariable Long id, HttpServletRequest requestObj) {
        Board board = boardRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);

        User user = null;
        try {
            String token = jwtUtil.resolveToken(requestObj);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        long likeCount = likeRepository.countByBoard(board);
        boolean liked = user != null && likeRepository.existsByUserAndBoard(user, board);

        return BoardResponse.of(board, user, likeCount, liked);
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
                .isDeleted(false)
                .build();

        long likeCount = 0;
        boolean liked = false;
        return BoardResponse.of(boardRepository.save(board), user, likeCount, liked);
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", security = @SecurityRequirement(name = "BearerAuth"))
    public BoardResponse updateBoard(@PathVariable Long id, @RequestBody BoardRequest request, HttpServletRequest requestObj) {
        String token = jwtUtil.resolveToken(requestObj);
        String username = jwtUtil.validateAndGetUsername(token);

        Board board = boardRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        long likeCount = likeRepository.countByBoard(board);
        boolean liked = likeRepository.existsByUserAndBoard(board.getUser(), board);
        return BoardResponse.of(boardRepository.save(board), board.getUser(), likeCount, liked);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public String deleteBoard(@PathVariable Long id, HttpServletRequest requestObj) {
        String token = jwtUtil.resolveToken(requestObj);
        String username = jwtUtil.validateAndGetUsername(token);

        Board board = boardRepository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        board.setDeleted(true);
        boardRepository.save(board);
        return "삭제 완료 (논리 삭제)";
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<String> toggleLike(@PathVariable Long id, HttpServletRequest req) {
        String username = jwtUtil.validateAndGetUsername(jwtUtil.resolveToken(req));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(id)
                .filter(b -> !b.isDeleted())
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

    @GetMapping("/search")
    @Operation(summary = "게시글 검색", description = "제목 또는 내용에 키워드가 포함된 게시글을 검색합니다.")
    public ResponseEntity<List<BoardResponse>> searchBoards(@RequestParam String keyword, HttpServletRequest request) {
        User user = null;
        try {
            String token = jwtUtil.resolveToken(request);
            String username = jwtUtil.validateAndGetUsername(token);
            user = userRepository.findByUsername(username).orElse(null);
        } catch (Exception ignored) {}

        List<Board> boards = boardRepository
                .findByIsDeletedFalseAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);

        User finalUser = user;
        List<BoardResponse> response = boards.stream()
                .map(board -> BoardResponse.of(
                        board,
                        finalUser,
                        likeRepository.countByBoard(board),
                        finalUser != null && likeRepository.existsByUserAndBoard(finalUser, board)
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

}
