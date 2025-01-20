package com.tension.gorani.translation.service;

import com.tension.gorani.translation.DTO.GlossaryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    @Value("${fastapi.url}") // FastAPI URL을 application.properties에서 가져옵니다.
    private String fastApiUrl;

    private final RestTemplate restTemplate;

    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            Map<String, String> requestBody = Map.of(
                "text", text,
                "source_lang", sourceLang,
                "target_lang", targetLang
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            System.out.println("Sending to FastAPI: " + requestBody);

            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl + "/translate/onlygpt", requestEntity, Map.class);

            System.out.println("Response from FastAPI: " + response.getBody());

            // FastAPI 응답에서 'answer' 키로 값 가져오기
            if (response.getBody() != null && response.getBody().get("answer") != null) {
                return response.getBody().get("answer").toString();
            } else {
                throw new RuntimeException("Missing 'answer' key in FastAPI response.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while communicating with FastAPI: " + e.getMessage());
        }
    }

    public void saveGlossary(GlossaryRequest glossaryRequest) {
        try {
            log.info("Saving glossary: {}", glossaryRequest);
            // fastApiUrl이 예: "http://localhost:8000"
            restTemplate.postForObject(fastApiUrl + "/api/glossary", glossaryRequest, Void.class);
        } catch (Exception e) {
            log.error("Error while saving glossary: {}", e.getMessage(), e);
            throw new RuntimeException("Error while saving glossary: " + e.getMessage());
        }
    }
    public List<Map<String, Object>> fetchUserGlossaries(int userId) {
        try {
            // FastAPI에 userId를 쿼리 파라미터로 전달
            String url = fastApiUrl + "/glossary?userId=" + userId;

            // List<Map<String, Object>> 형태로 응답 받음
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("FastAPI 응답 에러: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling FastAPI for userId={}: {}", userId, e.getMessage());
            throw new RuntimeException("FastAPI 호출 중 오류", e);
        }
    }
}
