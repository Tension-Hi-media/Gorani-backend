package com.tension.gorani.translation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

}
