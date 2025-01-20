package com.tension.gorani.auth.filter;

import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.auth.service.CustomUserDetails;
import com.tension.gorani.users.domain.entity.Users;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰을 생성하고 검증하는 클래스
    private final UserDetailsService userDetailsService; // 사용자 세부 정보를 로드하는 서비스

    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        List<String> roleLessList = Arrays.asList(
                // 토큰 사용하지 않아도 기능 수행할 수 있게 설정
                "/swagger-ui/(.*)",        //swagger 설정
                "/swagger-ui/index.html",  //swagger 설정
                "/v3/api-docs",              //swagger 설정
                "/v3/api-docs/(.*)",         //swagger 설정
                "/swagger-resources",        //swagger 설정
                "/swagger-resources/(.*)",    //swagger 설정
                "/auth/callback"
        );

        // 예외 URL 처리
        if (roleLessList.stream().anyMatch(uri -> Pattern.matches(uri, request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더에서 토큰 꺼내기
        String token = jwtTokenProvider.resolveToken(request); // 요청에서 JWT 토큰 추출
        log.info("추출한 토큰: {}", token);

        // 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token); // JWT에서 사용자 고유 넘버 추출

            // JWT에서 사용자 정보를 가져와서 Users 객체 생성
            Users users = Users.builder()
                    .id(Long.parseLong(claims.get("id").toString()))
                    .username(claims.get("username").toString())
                    .email(claims.get("email").toString())
                    .provider(claims.get("provider").toString())
                    .providerId(claims.get("provider_id").toString())
                    .isActive(Boolean.parseBoolean(claims.get("is_active").toString()))
                    .build();

            // 토큰에 담겨 있던 사용자 정보를 기반으로 CustomUserDetails 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails();
            userDetails.setUsers(users);

            // UsernamePasswordAuthenticationToken을 생성하고 인증 정보를 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }
}