package com.tension.gorani.translation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    @Value("${fastapi.url}")  // ✅ FastAPI의 기본 URL (모든 번역 처리)
    private String fastApiUrl;

    private final RestTemplate restTemplate;

    /**
     * ✅ 번역 요청을 FastAPI로 전달 (OpenAI, Gorani, LangGorani 처리)
     */
    public String translateText(String text, String sourceLang, String targetLang, String model) {
        try {
            return translateWithFastAPI(text, sourceLang, targetLang, model);
        } catch (Exception e) {
            log.error("❌ 번역 요청 실패: {}", e.getMessage(), e);
            return "번역 요청 실패";
        }
    }

    /**
     * ✅ FastAPI로 번역 요청 전달 (비동기 처리)
     */
    private String translateWithFastAPI(String text, String sourceLang, String targetLang, String model) {
        try {
            String url = fastApiUrl + "/translate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "text", text,
                    "source_lang", sourceLang,
                    "target_lang", targetLang,
                    "model", model
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("🔹 FastAPI로 번역 요청 보내기: {}", url);
            log.info("📦 요청 데이터: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            log.info("✅ FastAPI 응답: {}", response.getBody());

            if (response.getBody() != null && response.getBody().containsKey("task_id")) {
                String taskId = response.getBody().get("task_id").toString();
                return fetchTranslationResult(taskId);  // ✅ 비동기 작업 결과 조회 후 반환
            } else {
                return "FastAPI 응답이 올바르지 않습니다.";
            }
        } catch (Exception e) {
            log.error("❌ FastAPI 요청 오류: {}", e.getMessage(), e);
            return "FastAPI 요청 실패";
        }
    }

    /**
     * ✅ Celery 비동기 번역 작업의 상태를 조회하여 최종 번역 결과 반환
     */
    private String fetchTranslationResult(String taskId) {
        try {
            String url = String.format("%s/translate/status/%s", fastApiUrl, taskId);

            log.info("🔍 FastAPI 번역 상태 조회 시작: {}", url);

            int retries = 10; // ✅ 최대 10번(20초)까지 상태 확인
            while (retries-- > 0) {
                Thread.sleep(2000);

                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getBody() != null) {
                    String status = response.getBody().get("status").toString();

                    if ("completed".equals(status) && response.getBody().containsKey("result")) {
                        log.info("✅ 번역 완료: {}", response.getBody().get("result"));
                        return response.getBody().get("result").toString();
                    }
                }

                log.info("⏳ 번역 대기 중... ({}회 남음)", retries);
            }

            return "번역이 아직 완료되지 않았습니다. 나중에 다시 시도해주세요.";
        } catch (Exception e) {
            log.error("❌ FastAPI 상태 조회 오류: {}", e.getMessage(), e);
            return "FastAPI 상태 조회 실패";
        }
    }
}
