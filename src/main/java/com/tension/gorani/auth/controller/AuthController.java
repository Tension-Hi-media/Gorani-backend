package com.tension.gorani.auth.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.config.ResponseMessage;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final UsersRepository usersRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    @Value("${url.google.access-token}")
    private String accessTokenUrl;
    @Value("${url.google.profile}")
    private String profileUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${url.kakao.access-token}")
    private String kakaoAccessTokenUrl;


    @GetMapping("/auth/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(googleCallback(code));
        // 지금은 그냥 만들어집니다. code 추가하셔서 google client Id 인증 후 만들어주세요.
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        log.info("🦓🦓🦓🦓🦓🦓🦓🦓");
        try {
            // 1. 카카오에서 액세스 토큰을 요청
            String tokenUrl = kakaoAccessTokenUrl;  // 수정: kakaoAccessTokenUrl 사용
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", kakaoClientId);
            body.add("client_secret", kakaoClientSecret);
            body.add("redirect_uri", "http://localhost:3000/kakao"); // 프론트엔드 리디렉션 URI
            body.add("code", code);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

            // 2. 액세스 토큰을 추출
            String accessToken = extractAccessToken(response.getBody());

            // 3. 카카오 사용자 정보 요청
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userInfoRequestEntity = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequestEntity, String.class);

            // 4. 사용자 정보 처리
            String userInfo = userInfoResponse.getBody();
            log.info("카카오 사용자 정보: {}", userInfo);

            Users users = processKakaoUserInfo(userInfo);

            // 5. JWT 토큰 생성
            String backendAccessToken = jwtTokenProvider.generateToken(users); // 사용자 정보를 기반으로 JWT 생성

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("token", backendAccessToken);  // 수정: 결과에 token을 직접 추가
            responseMap.put("user", users);

            return ResponseEntity
                    .ok()
                    .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap)); // 백엔드 액세스 토큰 반환
        } catch (Exception e) {
            log.error("카카오 로그인 처리 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 실패", null));
        }
    }


    private String extractAccessToken(String responseBody) {
        // JSON 파싱을 통해 access token 추출
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonObject.get("access_token").getAsString();
    }

    private Users processGoogleUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            String providerId = jsonNode.get("sub").asText(); // 구글 ID를 providerId로 사용
            String name = jsonNode.get("name").asText(); // 사용자 이름
            String email = jsonNode.get("email").asText(); // 이메일

            // providerId로 사용자 찾기
            Users user = usersRepository.findByProviderId(providerId);
            if (user == null) {
                // 사용자 정보가 없으면 새로운 사용자 생성
                user = new Users();
                user.setProviderId(providerId);
                user.setUsername(name);
                user.setEmail(email);
                usersRepository.save(user); // 데이터베이스에 저장
            }
            log.info("user 정보 : {}", user);
            return user; // 사용자 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }


    private Users processKakaoUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            // providerId를 임의로 "kakao"로 설정
            String providerId = "kakao"; // 여기서 providerId를 "kakao"로 설정
            log.info("providerId : {}", providerId);

            // kakao_account에서 사용자 이름과 이메일 정보 가져오기
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            String nickName = kakaoAccount.get("profile").get("nickname").asText(); // 사용자 닉네임
            log.info("nickName : {}", nickName);
            String email = kakaoAccount.get("email").asText(); // 이메일
            log.info("email : {}", email);

            // providerId로 사용자 찾기
            Users user = usersRepository.findByProviderId(providerId);
            if (user == null) {
                // 사용자 정보가 없으면 새로운 사용자 생성
                user = new Users();
                user.setProviderId(providerId);
                user.setUsername(nickName);
                user.setEmail(email);
                usersRepository.save(user); // 데이터베이스에 저장
            }
            log.info("user 정보 : {}", user);
            return user; // 사용자 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }



}