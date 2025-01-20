package com.tension.gorani.users.controller;

import com.tension.gorani.common.ResponseMessage;
import com.tension.gorani.users.domain.dto.UpdateCompanyResponse;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
