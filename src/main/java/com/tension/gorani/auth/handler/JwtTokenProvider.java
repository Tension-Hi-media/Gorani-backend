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
    public String generateToken(Users user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("provider", user.getProvider());
        claims.put("provider_id", user.getProviderId());
        claims.put("is_active", user.getIsActive());
        
        if (user.getCompany() != null) {
            claims.put("company_id", user.getCompany().getCompanyId());
            claims.put("company_name", user.getCompany().getName());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
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