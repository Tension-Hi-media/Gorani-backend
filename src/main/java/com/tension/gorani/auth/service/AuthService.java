package com.tension.gorani.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public Map<String, Object> handleGoogleCallback(String token) throws Exception {
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";

        // 사용자 정보 요청
        Map<String, Object> userInfo = restTemplate.getForObject(
                userInfoEndpoint + "?access_token=" + token, Map.class
        );

        if (userInfo == null || userInfo.isEmpty()) {
            throw new Exception("Google 사용자 정보를 가져올 수 없습니다.");
        }

        return userInfo; // 사용자 정보를 반환
    }
    public Map<String, Object> handleKakaoCallback(String code) throws Exception {
        return handleOAuthCallback(
                code,
                "https://kauth.kakao.com/oauth/token",
                "https://kapi.kakao.com/v2/user/me",
                System.getenv("KAKAO_CLIENT_ID"),
                System.getenv("KAKAO_CLIENT_SECRET"),
                System.getenv("KAKAO_REDIRECT_URI")
        );
    }

    private Map<String, Object> handleOAuthCallback(String code, String tokenUrl, String userInfoUrl,
                                                    String clientId, String clientSecret, String redirectUri) throws Exception {
        String accessToken = getAccessToken(code, clientId, clientSecret, redirectUri, tokenUrl);
        String userInfo = getUserInfo(accessToken, userInfoUrl);

        Users user = processUserInfo(userInfo, userInfoUrl.contains("kakao"));
        String backendAccessToken = jwtTokenProvider.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", backendAccessToken);
        response.put("user", user);
        return response;
    }

    private String getAccessToken(String code, String clientId, String clientSecret, String redirectUri, String tokenUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

        return extractAccessToken(response.getBody());
    }

    private String getUserInfo(String accessToken, String userInfoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }

    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Access token 추출 실패: {}", e.getMessage());
            throw new RuntimeException("Access token 추출 실패", e);
        }
    }

    private Users processUserInfo(String userInfo, boolean isKakao) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(userInfo);

        String providerId = isKakao ? jsonNode.get("id").asText() : jsonNode.get("sub").asText();
        String name = isKakao
                ? jsonNode.get("kakao_account").get("profile").get("nickname").asText()
                : jsonNode.get("name").asText();
        String email = isKakao
                ? jsonNode.get("kakao_account").get("email").asText()
                : jsonNode.get("email").asText();

        return saveOrUpdateUser(providerId, name, email);
    }

    private Users saveOrUpdateUser(String providerId, String name, String email) {
        Users user = usersRepository.findByProviderId(providerId);
        if (user == null) {
            user = new Users();
            user.setProviderId(providerId);
            user.setUsername(name);
            user.setEmail(email);
            usersRepository.save(user);
        }
        return user;
    }
}
