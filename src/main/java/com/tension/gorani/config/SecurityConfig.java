package com.tension.gorani.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ✅ CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CORS 설정 적용
                .authorizeHttpRequests(auth -> auth
                        // ✅ 인증 없이 접근 가능한 엔드포인트 (permitAll)
                        .requestMatchers(
                                "/api/v1/auth/**",   // 로그인 & 회원가입 관련 API
                                "/api/v1/company/**" // 기업 관련 API
                        ).permitAll()

                        // ✅ 인증 필요 (로그인한 사용자만 접근 가능)
                        .requestMatchers(
                                "/api/v1/user/mypage", // 마이페이지 조회
                                "/api/v1/user/updateCompany" // 기업 정보 업데이트
                        ).authenticated()

                        // ✅ 그 외 모든 요청 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*")); // ✅ 모든 Origin 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
