package com.tension.gorani.users.controller;

import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "user")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입

    @PostMapping("/save-or-update")
    public ResponseEntity<Users> saveOrUpdateUser(@RequestParam String providerId,
                                                  @RequestParam String email,
                                                  @RequestParam String username,
                                                  @RequestParam String provider) {
        log.info("API call to save or update user: providerId={}, email={}, username={}, provider={}", providerId, email, username, provider);
        Users user = userService.saveOrUpdateUser(providerId, email, username, provider);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<Users> getCurrentUser(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            Long userId = claims.get("id", Long.class);
            Users user = userService.findById(userId); // 사용자 ID로 사용자 정보 조회
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
