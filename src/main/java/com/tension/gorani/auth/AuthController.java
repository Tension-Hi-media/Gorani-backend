package com.tension.gorani.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("🦓 Google OAuth callback initiated with code: {}", code);

        // 1. Access Token 요청
        String accessToken;
        try {
            accessToken = requestGoogleAccessToken(code);
            log.info("Google Access Token: {}", accessToken);
        } catch (Exception e) {
            log.error("Failed to retrieve Google Access Token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "구글 액세스 토큰 요청 실패"));
        }

        // 2. 사용자 정보 요청
        String userInfo;
        try {
            userInfo = requestGoogleUserInfo(accessToken);
            log.info("Google User Info: {}", userInfo);
        } catch (Exception e) {
            log.error("Failed to retrieve Google User Info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "구글 사용자 정보 요청 실패"));
        }

        // 3. 사용자 정보 처리
        Users users;
        try {
            users = processGoogleUserInfo(userInfo);
            if (users == null) {
                throw new IllegalArgumentException("User processing failed");
            }
        } catch (Exception e) {
            log.error("Failed to process Google User Info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보 처리 실패"));
        }

        // 4. 백엔드 서버 Access Token 생성
        String backendAccessToken = jwtTokenProvider.generateToken(users);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", backendAccessToken);
        responseMap.put("user", users);

        log.info("Backend Access Token: {}", backendAccessToken);

        return ResponseEntity
                .ok()
                .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap));
    }

    private String requestGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        log.info("Request to Google Access Token API with redirect_uri: {}", redirectUri);
        log.info("Request Body: {}", body);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        log.info("Request to Google Access Token API: {}", requestEntity);

        ResponseEntity<String> response = restTemplate.exchange(accessTokenUrl, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to retrieve Google Access Token");
        }

        return extractAccessToken(response.getBody());
    }

    private String requestGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(profileUrl, HttpMethod.GET, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to retrieve Google User Info");
        }

        return response.getBody();
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {

        log.info("🦓🦓enter");
        // 1. 카카오에 access token 요청
        String tokenUrl = kakaoAccessTokenUrl;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("client_secret", kakaoClientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        log.info(String.valueOf(requestEntity));
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

        // 2. 액세스 토큰 반환
        String accessToken = extractAccessToken(response.getBody());
        log.info("accessToken : {}", accessToken);


        // 3. 사용자 정보 요청
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers2.setBearerAuth(accessToken);
        HttpEntity<String> requestEntity2 = new HttpEntity<>(headers2);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity2, String.class);

        // 4. 사용자 정보 처리 및 회원가입 로직
        String userInfo = userInfoResponse.getBody();
        log.info("userInfo: {}", userInfo);

        Users users = processKakaoUserInfo(userInfo);

        // 5. 백엔드 서버 access token 생성하여 프론트 서버로 전달
        String backendAccessToken = jwtTokenProvider.generateToken(users); // 사용자 정보를 기반으로 JWT 생성

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", backendAccessToken);
        responseMap.put("user", users);

        log.info("backendAccessToken : {}", backendAccessToken);

        return ResponseEntity
                .ok()
                .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap)); // 백엔드 액세스 토큰 반환
    }

    private String extractAccessToken(String responseBody) {
        // JSON 파싱을 통해 access token 추출
        try {
            // Jackson ObjectMapper를 사용하여 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // access_token을 추출
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
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
            log.error("Failed to process Google user info", e);
            throw new RuntimeException("Failed to process Google user info", e);
        }
    }

    private Users processKakaoUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            String providerId = jsonNode.get("id").asText(); // Kakao ID를 providerId로 사용
            log.info("providerId : {}", providerId);

            // kakao_account에서 사용자 이름과 이메일 정보 가져오기
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            String name = kakaoAccount.get("name").asText(); // 사용자 이름
            log.info("name : {}", name);
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