package com.tension.gorani.translation.controller;

import com.tension.gorani.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Translation")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/translation")
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("")
    public ResponseEntity<?> translate(@RequestBody Map<String, String> request) {
        try {
            // ✅ 입력 값 검증 (text 필수)
            if (!request.containsKey("text") || request.get("text").isBlank()) {
                return ResponseEntity.badRequest().body("❌ 'text' 값이 필요합니다.");
            }

            String text = request.get("text");
            String sourceLang = request.getOrDefault("sourceLang", "ko");
            String targetLang = request.getOrDefault("targetLang", "en");
            String model = request.getOrDefault("model", "OpenAI"); // ✅ 기본값 OpenAI (FastAPI에서 처리)

            log.info("🔹 번역 요청 - Text: {}, Source: {}, Target: {}, Model: {}", text, sourceLang, targetLang, model);

            // ✅ FastAPI로 번역 요청 (OpenAI도 FastAPI에서 수행)
            String translatedText = translationService.translateText(text, sourceLang, targetLang, model);

            log.info("✅ 번역 완료 - Result: {}", translatedText);

            return ResponseEntity.ok(Map.of("translated_text", translatedText));
        } catch (Exception e) {
            log.error("❌ 번역 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Translation failed: " + e.getMessage());
        }
    }
}
