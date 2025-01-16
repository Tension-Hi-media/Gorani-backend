package com.tension.gorani.auth;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.config.ResponseMessage;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import com.tension.gorani.users.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final UsersRepository usersRepository;
    private final UserService userService; // UserService 주입

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

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    @GetMapping("/auth/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(googleCallback(code));
        // 지금은 그냥 만들어집니다. code 추가하셔서 google client Id 인증 후 만들어주세요.
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
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
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

    @GetMapping("/auth/naver/callback")
    public ResponseEntity<?> naverCallback(@RequestParam("code") String code,
                              @RequestParam("state") String state,
                              HttpServletResponse response) throws IOException {
        log.info("Received callback request. Code: {}, State: {}", code, state);

        // 1. 액세스 토큰 요청
        String accessToken = requestNaverAccessToken(code, state);
        log.info("Access Token: {}", accessToken);

        // 2. 사용자 정보 요청
        Map<String, String> userInfo = requestNaverUserInfo(accessToken);
        log.info("User Info: {}", userInfo);

        // 3. 사용자 정보 저장 또는 업데이트
        String name = userInfo.get("name") != null ? userInfo.get("name") : userInfo.get("nickname");
        Users user = processNaverUserInfo(
                userInfo.get("id"),
                userInfo.get("email"),
                name
        );

        // 4. JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user);
        log.info("Generated JWT Token: {}", token);

        // 5. React로 리다이렉트
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", token);
        responseMap.put("user", user);

        return ResponseEntity
                .ok()
                .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap));

    }

    private String requestNaverAccessToken(String code, String state) {
        try {
            String tokenUrl = "https://nid.naver.com/oauth2.0/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", naverClientId);
            body.add("client_secret", naverClientSecret);
            body.add("redirect_uri", naverRedirectUri);
            body.add("code", code);
            body.add("state", state);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            log.info("Response Status Code: {}", response.getStatusCode());
            log.info("Naver Access Token Response: {}", response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("error")) {
                throw new RuntimeException("Naver Login Error: " + jsonNode.get("error_description").asText());
            }

            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing Naver access token response", e);
        } catch (Exception e) {
            log.error("Error during Naver access token request", e);
            throw new RuntimeException("Error during Naver access token request", e);
        }
    }

    // 사용자 정보 요청
    private Map<String, String> requestNaverUserInfo(String accessToken) {
        try {
            String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

            log.info("User Info Response: {}", response.getBody()); // 응답 로그 출력

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (!jsonNode.has("response")) {
                throw new RuntimeException("Naver user info not found.");
            }

            JsonNode responseNode = jsonNode.get("response");
            log.debug("Received response: {}", response.getBody());
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("id", responseNode.get("id").asText());
            userInfo.put("email", responseNode.has("email") ? responseNode.get("email").asText() : "unknown@naver.com");
            userInfo.put("name", responseNode.has("name") ? responseNode.get("name").asText() : "Unknown");

            return userInfo;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing user info response", e);
        }
    }

    // 사용자 정보 저장 및 처리
    private Users processNaverUserInfo(String naverId, String email, String name) {
        try {
            log.info("Processing Naver user info: naverId={}, email={}, name={}", naverId, email, name);
            return userService.saveOrUpdateUser(naverId, email, name, "naver");
        } catch (Exception e) {
            log.error("사용자 정보 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("사용자 정보 저장 중 오류 발생");
        }
    }

    @GetMapping("/auth/login/naver")
    public void naverLogin(HttpServletResponse response, HttpSession session) throws IOException {
        String state = UUID.randomUUID().toString(); // CSRF 방지를 위한 상태값
        session.setAttribute("state", state); // 세션에 상태값 저장
        String redirectUri = URLEncoder.encode("http://localhost:3000/naver-success", "UTF-8");
        String naverLoginUrl = String.format(
                "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
                clientId, redirectUri, state
        );

        log.info("Generated Naver Login URL: {}", naverLoginUrl); // 네이버 로그인 URL 로그 출력
        response.sendRedirect(naverLoginUrl);
    }
}