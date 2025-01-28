package com.tension.gorani.auth.controller;

import com.tension.gorani.auth.dto.NaverLoginRequest;
import com.tension.gorani.auth.service.AuthService;
import com.tension.gorani.config.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth") // 예시로 /api/auth prefix
public class AuthController {

    private final AuthService authService;

    /**
     * [방법 A] 프론트엔드에서 code, state를 받아와 POST로 호출
     * 요청 예: POST /api/auth/naver
     * Body: { "code": "...", "state": "..." }
     */
    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestBody NaverLoginRequest request) {
        try {
            log.info("Naver Login Request: code={}, state={}", request.getCode(), request.getState());

            // AuthService에서 네이버 API와 통신해 토큰 발급 및 JWT 생성 등 처리
            Map<String, Object> result = authService.handleOAuthCallback(request.getCode(), "naver");

            // 토큰, 유저 정보 등을 담아 응답
            return ResponseEntity.ok(
                    new ResponseMessage(HttpStatus.CREATED, "로그인 성공", result)
            );
        } catch (Exception e) {
            log.error("Naver Login Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "네이버 로그인 오류",
                            null
                    ));
        }
    }

    /*
     * 기존 GET /naver-success 제거(혹은 주석 처리)
     * 프론트엔드(React) 콜백 방식을 쓸 경우 필요하지 않습니다.
     */
    // @GetMapping("/naver-success")
    // public ResponseEntity<?> naverCallback(...) { ... }

    /*
     * 다른 OAuth (구글, 카카오 등)도 동일하게
     * POST /api/auth/google 등으로 구성해주면 됩니다.
     */
}
