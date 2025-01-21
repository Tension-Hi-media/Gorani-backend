package com.tension.gorani.users.controller;

import com.tension.gorani.auth.service.CustomUserDetails;
import com.tension.gorani.common.ResponseMessage;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "user")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/save-or-update")
    public ResponseEntity<Users> saveOrUpdateUser(@RequestParam String providerId,
                                                  @RequestParam String email,
                                                  @RequestParam String username,
                                                  @RequestParam String provider) {
        log.info("API call to save or update user: providerId={}, email={}, username={}, provider={}", providerId, email, username, provider);
        Users user = userService.saveOrUpdateUser(providerId, email, username, provider);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/updateCompany")
    public ResponseEntity<ResponseMessage> updateCompany(@RequestParam Long userId,
                                                  @RequestParam Long companyId) {
        log.info("API call to save or update user: userId={}, companyId={}", userId, companyId);
        Users user = userService.updateUserWithCompany(userId, companyId);

        Map<String, Object> responseMap = new HashMap<>();

        responseMap.put("user", user);

        return ResponseEntity.ok()
                .body(new ResponseMessage(HttpStatus.OK,"기업 등록 성공",responseMap));
    }

    @GetMapping("/mypage")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("Fetching user info for user: {}", customUserDetails.getUsername());
        
        Users user = customUserDetails.getUsers(); // CustomUserDetails에서 사용자 정보 가져오기
        log.info("Retrieved user: {}", user); // 사용자 객체 로깅
        
        if (user == null) {
            log.warn("User object is null");
            return ResponseEntity.notFound().build();
        }

        log.info("User email: {}, username: {}", user.getEmail(), user.getUsername()); // 구체적인 필드 값 로깅

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("email", user.getEmail());
        responseMap.put("name", user.getUsername());
        
        // 기업 정보 추가
        if (user.getCompany() != null) {
            responseMap.put("companyName", user.getCompany().getName());
            responseMap.put("registrationNumber", user.getCompany().getRegistrationNumber());
            responseMap.put("representativeName", user.getCompany().getRepresentativeName());
        } else {
            responseMap.put("companyName", null);
            responseMap.put("registrationNumber", null);
            responseMap.put("representativeName", null);
        }

        log.info("Response map: {}", responseMap); // 최종 응답 데이터 로깅
        return ResponseEntity.ok(responseMap);
    }
}
