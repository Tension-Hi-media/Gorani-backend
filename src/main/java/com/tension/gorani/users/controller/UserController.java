package com.tension.gorani.users.controller;

import com.tension.gorani.auth.service.CustomUserDetails;
import com.tension.gorani.common.ResponseMessage;
import com.tension.gorani.users.domain.dto.UserResponseDTO;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "user")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    // ✅ 유저 저장 또는 업데이트 (소셜 로그인 시 사용)
    @PostMapping("/save-or-update")
    public ResponseEntity<UserResponseDTO> saveOrUpdateUser(@RequestParam String providerId,
                                                            @RequestParam String email,
                                                            @RequestParam String username,
                                                            @RequestParam String provider) {
        log.info("📢 API 호출: 유저 저장 또는 업데이트 | providerId={}, email={}, username={}, provider={}", providerId, email, username, provider);

        Users user = userService.saveOrUpdateUser(providerId, email, username, provider);
        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    // ✅ 특정 유저의 회사 정보 업데이트 (기업 등록 후 호출)
    @PostMapping("/updateCompany")
    public ResponseEntity<ResponseMessage> updateCompany(@RequestParam Long userId,
                                                         @RequestParam Long companyId) {
        log.info("📢 API 호출: 유저의 기업 정보 업데이트 | userId={}, companyId={}", userId, companyId);

        Users user = userService.updateUserWithCompany(userId, companyId);
        return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK, "✅ 기업 등록 성공", (Map<String, Object>) UserResponseDTO.from(user)));
    }

    // ✅ 로그인한 유저의 마이페이지 정보 조회
    @GetMapping("/mypage")
    public ResponseEntity<UserResponseDTO> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        log.info("📢 API 호출: 마이페이지 조회 | user={}", customUserDetails.getUsername());

        Users user = customUserDetails.getUserInfo();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    // ✅ 특정 유저 정보 조회 (ID 기준)
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        log.info("📢 API 호출: 유저 정보 조회 | userId={}", userId);

        Users user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 유저를 찾을 수 없습니다. userId=" + userId));

        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

}
