package com.tension.gorani.auth.controller;

import com.tension.gorani.auth.service.AuthService;
import com.tension.gorani.config.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/callback")
    public ResponseEntity<?> oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("provider") String provider) {
        try {
            Map<String, Object> response = authService.handleOAuthCallback(code, provider);
            return ResponseEntity.ok(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, provider + " 로그인 오류", null));
        }
    }

}