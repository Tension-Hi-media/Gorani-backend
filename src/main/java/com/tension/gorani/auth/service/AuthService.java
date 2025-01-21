package com.tension.gorani.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tension.gorani.auth.handler.JwtTokenProvider;
import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
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
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
        log.info("accessToken: {}", response.getBody());
        return extractAccessToken(response.getBody());
    }

    private Map<String, Object> getUserInfo(String accessToken, String userInfoUrl) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, String.class);
        log.info("userInfo: {}", response.getBody());
        return parseUserInfo(response.getBody());
    }

    private Map<String, Object> parseUserInfo(String userInfoResponse) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(userInfoResponse);
        Map<String, Object> userInfo = new HashMap<>();

        if (jsonNode.has("id")) {
            userInfo.put("providerId", jsonNode.get("id").asText());
        } else if (jsonNode.has("sub")) {
            userInfo.put("providerId", jsonNode.get("sub").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("providerId", jsonNode.get("response").get("id").asText());
        }

        if (jsonNode.has("name")) {
            userInfo.put("name", jsonNode.get("name").asText());
        } else if (jsonNode.has("kakao_account")) {
            userInfo.put("name", jsonNode.get("kakao_account").get("name").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("name", jsonNode.get("response").get("name").asText());
        }

        if (jsonNode.has("email")) {
            userInfo.put("email", jsonNode.get("email").asText());
        } else if (jsonNode.has("kakao_account")) {
            userInfo.put("email", jsonNode.get("kakao_account").get("email").asText());
        } else if (jsonNode.has("response")) {
            userInfo.put("email", jsonNode.get("response").get("email").asText());
        }

        log.info("userInfo: {}", userInfo);

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
        Users user = usersRepository.findByProviderId(providerId);
        if (user == null) {
            user = new Users();
            user.setProviderId(providerId);
            user.setUsername(name);
            user.setEmail(email);
            user.setProvider(provider);
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