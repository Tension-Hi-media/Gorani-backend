package com.tension.gorani.translation.controller;

import com.tension.gorani.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Translation")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/translation")
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("")
    public ResponseEntity<?> translate(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String sourceLang = request.getOrDefault("sourceLang", "ko"); // Source는 한국어
            String targetLang = request.getOrDefault("targetLang", "en"); // Target은 영어

            // 로깅
            System.out.println("Text: " + text);
            System.out.println("Source Language: " + sourceLang);
            System.out.println("Target Language: " + targetLang);

            // FastAPI로 번역 요청
            String translatedText = translationService.translateText(text, sourceLang, targetLang);

            System.out.println("Translated Text: " + translatedText);

            return ResponseEntity.ok(Map.of("translated_text", translatedText));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Translation failed: " + e.getMessage());
        }
    }

}