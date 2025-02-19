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

    // ✅ 사용자 저장 또는 업데이트
    @PostMapping("/save-or-update")
    public ResponseEntity<Users> saveOrUpdateUser(@RequestParam String providerId,
                                                  @RequestParam String email,
                                                  @RequestParam String username,
                                                  @RequestParam String provider) {
        log.info("📌 [saveOrUpdateUser] providerId={}, email={}, username={}, provider={}",
                providerId, email, username, provider);
        Users user = userService.saveOrUpdateUser(providerId, email, username, provider);
        return ResponseEntity.ok(user);
    }

    // ✅ 사용자의 회사 정보 업데이트 (company_id 설정)
    @PostMapping("/updateCompany")
    public ResponseEntity<ResponseMessage> updateCompany(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long companyId = request.get("companyId");

        log.info("📌 [updateCompany] 요청: userId={}, companyId={}", userId, companyId);

        if (userId == null || companyId == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseMessage(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", null)
            );
        }

        Users user = userService.updateUserWithCompany(userId, companyId);

        // ✅ 사용자 및 기업 정보 응답에 포함
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", user.getId());
        responseMap.put("username", user.getUsername());
        responseMap.put("email", user.getEmail());
        responseMap.put("provider", user.getProvider());
        responseMap.put("providerId", user.getProviderId());
        responseMap.put("isActive", user.getIsActive());

        if (user.getCompany() != null) {
            responseMap.put("company", Map.of(
                    "companyId", user.getCompany().getCompanyId(),
                    "name", user.getCompany().getName(),
                    "registrationNumber", user.getCompany().getRegistrationNumber(),
                    "representativeName", user.getCompany().getRepresentativeName()
            ));
        } else {
            responseMap.put("company", null);
        }

        log.info("✅ [updateCompany] 완료: {}", responseMap);
        return ResponseEntity.ok(new ResponseMessage(HttpStatus.OK, "기업 등록 성공", responseMap));
    }

    // ✅ 마이페이지 사용자 정보 가져오기
    @GetMapping("/mypage")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("Fetching user info for user: {}", customUserDetails.getUsername());

        Users user = customUserDetails.getUserInfo();

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", user.getId());
        responseMap.put("username", user.getUsername());
        responseMap.put("email", user.getEmail());
        responseMap.put("provider", user.getProvider());
        responseMap.put("providerId", user.getProviderId());
        responseMap.put("isActive", user.getIsActive());
        responseMap.put("createdAt", user.getCreatedAt());
        responseMap.put("updatedAt", user.getUpdatedAt());

        // ✅ 기업 정보 추가
        if (user.getCompany() != null) {
            responseMap.put("company", Map.of(
                    "companyId", user.getCompany().getCompanyId(),
                    "name", user.getCompany().getName(),
                    "registrationNumber", user.getCompany().getRegistrationNumber(),
                    "representativeName", user.getCompany().getRepresentativeName()
            ));
        } else {
            responseMap.put("company", null);
        }

        log.info("Response map: {}", responseMap);
        return ResponseEntity.ok(responseMap);
    }
}
