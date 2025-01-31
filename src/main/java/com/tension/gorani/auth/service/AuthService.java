package com.tension.gorani.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${url.google.access-token}")
    private String googleAccessTokenUrl;
    @Value("${url.google.profile}")
    private String googleProfileUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${url.kakao.access-token}")
    private String kakaoAccessTokenUrl;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    public Map<String, Object> handleOAuthCallback(String code, String provider) throws Exception {
        String tokenUrl;
        String userInfoUrl;
        String clientId;
        String clientSecret;
        String redirectUri;

        switch (provider.toLowerCase()) {
            case "google":
                tokenUrl = googleAccessTokenUrl;
                userInfoUrl = googleProfileUrl;
                clientId = googleClientId;
                clientSecret = googleClientSecret;
                redirectUri = googleRedirectUri;
                break;
            case "kakao":
                tokenUrl = kakaoAccessTokenUrl;
                userInfoUrl = "https://kapi.kakao.com/v2/user/me";
                clientId = kakaoClientId;
                clientSecret = kakaoClientSecret;
                redirectUri = kakaoRedirectUri;
                break;
            case "naver":
                tokenUrl = "https://nid.naver.com/oauth2.0/token";
                userInfoUrl = "https://openapi.naver.com/v1/nid/me";
                clientId = naverClientId;
                clientSecret = naverClientSecret;
                redirectUri = naverRedirectUri;
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        String accessToken;
        accessToken = getAccessToken(code, clientId, clientSecret, redirectUri, tokenUrl);
        Map<String, Object> userInfo = getUserInfo(accessToken, userInfoUrl);
        return processUserInfo(userInfo, provider);
    }

    private String getAccessToken(String code, String clientId, String clientSecret, String redirectUri, String tokenUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);

        // 🔥 카카오는 client_secret 필요 없음
        if (!tokenUrl.contains("kakao")) {
            body.add("client_secret", clientSecret);
        }

        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
        log.info("Access Token Response: {}", response.getBody());
        return extractAccessToken(response.getBody());
    }


    private Map<String, Object> getUserInfo(String accessToken, String userInfoUrl) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // ✅ Bearer 토큰 추가

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, String.class);

        log.info("User Info Response: {}", response.getBody());
        return parseUserInfo(response.getBody());
    }

    private Map<String, Object> parseUserInfo(String userInfoResponse) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(userInfoResponse);
        Map<String, Object> userInfo = new HashMap<>();

        // ✅ 1. 사용자 ID 가져오기 (providerId)
        if (jsonNode.has("id")) {
            userInfo.put("providerId", jsonNode.get("id").asText());
        } else if (jsonNode.has("sub")) {
            userInfo.put("providerId", jsonNode.get("sub").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("providerId", jsonNode.get("response").get("id").asText());
        }

        // ✅ 2. 사용자 이름 (카카오: profile.nickname)
        if (jsonNode.has("kakao_account") && jsonNode.get("kakao_account").has("profile")) {
            JsonNode profile = jsonNode.get("kakao_account").get("profile");
            if (profile.has("nickname")) {
                userInfo.put("name", profile.get("nickname").asText()); // ✅ 이름 저장
            }
        } else if (jsonNode.has("name")) {
            userInfo.put("name", jsonNode.get("name").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("name", jsonNode.get("response").get("name").asText());
        }

        // ✅ 3. 사용자 이메일 (카카오: kakao_account.email)
        if (jsonNode.has("kakao_account") && jsonNode.get("kakao_account").has("email")) {
            userInfo.put("email", jsonNode.get("kakao_account").get("email").asText());
        } else if (jsonNode.has("email")) {
            userInfo.put("email", jsonNode.get("email").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("email", jsonNode.get("response").get("email").asText());
        }

        log.info("Parsed Kakao User Info: {}", userInfo);

        return userInfo;
    }

    private Map<String, Object> processUserInfo(Map<String, Object> userInfo, String provider) {
        String providerId = (String) userInfo.get("providerId");
        String name = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");

        Users user = saveOrUpdateUser(providerId, name, email, provider);

        String backendAccessToken = jwtTokenProvider.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", backendAccessToken);
        response.put("user", user);
        return response;
    }

    private Users saveOrUpdateUser(String providerId, String name, String email, String provider) {
        Users user = usersRepository.findByProviderId(providerId); // 기존 사용자 조회
        if (user == null) {
            // 새 사용자 생성 및 저장
            user = Users.builder()
                    .providerId(providerId)
                    .username(name)
                    .email(email)
                    .provider(provider)
                    .isActive(true) // 기본값 설정
                    .build();
            usersRepository.save(user);
        }
        return user;
    }

    private String extractAccessToken(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Access token 추출 실패: {}", e.getMessage());
            throw new RuntimeException("Access token 추출 실패", e);
        }
    }
}