package com.gbsw.gbsw.controller;

import com.gbsw.gbsw.dto.UserRequest;
import com.gbsw.gbsw.dto.UserResponse;
import com.gbsw.gbsw.entity.User;
import com.gbsw.gbsw.repository.UserRepository;
import com.gbsw.gbsw.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "username과 password를 받아 회원가입합니다.",
            security = {}
    )
    public String signup(@RequestBody UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .build();
        userRepository.save(newUser);

        return "회원가입 성공";
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "username과 password를 받아 JWT 토큰을 반환합니다.",
            security = {}
    )
    public UserResponse login(@RequestBody UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new UserResponse(token);
    }
}
