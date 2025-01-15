package com.tension.gorani.auth.handler;

import com.tension.gorani.users.domain.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    // JWT 토큰 생성
    public String generateToken(Users users) {
        Claims claims = Jwts.claims().setSubject(users.getEmail()); // 사용자 이메일을 주제로 설정
        claims.put("username", users.getUsername()); // 토큰에 사용자 이름 넣기
        claims.put("id", users.getId());

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime); // 만료 시간 설정

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // 비밀 키로 서명
                .compact();
    }

    // HTTP 요청에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token); // 서명 키로 검증
            return true; // 유효한 토큰
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
            return false; // 만료된 토큰
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
            return false; // 유효하지 않은 서명
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return false; // 기타 등등
        }
    }

    // JWT에서 클레임 추출
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
}
