package com.gbsw.gbsw.config;

import com.gbsw.gbsw.config.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",              // 로그인, 토큰 발급 등
                                "/api/user/**",              // 회원가입, 사용자 관련
                                "/api/board",                // 게시글 목록
                                "/api/board/**",             // 게시글 단건 조회 등
                                "/v3/api-docs/**",           // Swagger 문서
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**"
                        ).permitAll()

                        .requestMatchers("/api/report/admin").hasRole("ADMIN")   // 관리자용 신고 조회
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")       // 기타 관리자 전용

                        .requestMatchers("/api/report/**").authenticated()       // 일반 신고 등록은 인증만

                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
