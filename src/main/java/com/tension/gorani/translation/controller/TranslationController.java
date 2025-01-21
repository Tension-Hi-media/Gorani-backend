package com.tension.gorani.translation.controller;

import com.tension.gorani.translation.DTO.GlossaryRequest;
import com.tension.gorani.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Operation(summary = "용어집 저장", description = "새로운 용어집을 저장합니다.")
    @PostMapping("/glossary")
    public ResponseEntity<GlossaryRequest> saveGlossary(@RequestBody GlossaryRequest glossaryRequest) {
        try {
            // 로그로 확인
            log.info("Saving glossary: {}", glossaryRequest);

            // 예: MySQL로부터 userId를 읽어와야 한다면,
            // glossaryRequest.setUserId(어딘가에서 읽어온 값);
            // 이런 식으로 강제로 설정도 가능.

            // 현재 구조대로라면, FastAPI에 glossaryRequest를 그대로 전달
            translationService.saveGlossary(glossaryRequest);

            // glossaryRequest를 그대로 응답
            return ResponseEntity.ok(glossaryRequest);
        } catch (Exception e) {
            log.error("Failed to save glossary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/glossary")
    public ResponseEntity<?> getGlossaries(@RequestParam int userId) {
        try {
            // userId로 필터링된 용어집 목록을 FastAPI 또는 DB에서 조회
            List<Map<String, Object>> glossaries = translationService.fetchUserGlossaries(userId);
            return ResponseEntity.ok(glossaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("용어집 조회 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "용어집 삭제", description = "특정 용어집을 삭제합니다.")
    @DeleteMapping("/glossary/{id}")
    public ResponseEntity<?> deleteGlossary(@PathVariable String id) {
        try {
            translationService.deleteGlossary(id);
            return ResponseEntity.ok(Map.of("message", "Glossary deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete glossary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete glossary: " + e.getMessage());
        }
    }

    @Operation(summary = "단어쌍 추가", description = "특정 용어집에 단어쌍을 추가합니다.")
    @PostMapping("/glossary/{id}/word-pair")
    public ResponseEntity<?> addWordPair(@PathVariable String id, @RequestBody GlossaryRequest.WordPair wordPair) {
        try {
            translationService.addWordPair(id, wordPair);
            return ResponseEntity.ok(Map.of("message", "Word pair added successfully"));
        } catch (Exception e) {
            log.error("Failed to add word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add word pair: " + e.getMessage());
        }
    }

    @Operation(summary = "단어쌍 수정", description = "특정 용어집의 단어쌍을 수정합니다.")
    @PutMapping("/glossary/{glossaryId}/word-pair/{wordPairId}")
    public ResponseEntity<?> updateWordPair(
            @PathVariable String glossaryId,
            @PathVariable String wordPairId,
            @RequestBody GlossaryRequest.WordPair updatedWordPair) {
        try {
            log.info("Updating word pair: glossaryId={}, wordPairId={}, updatedWordPair={}", glossaryId, wordPairId, updatedWordPair);

            translationService.updateWordPair(glossaryId, wordPairId, updatedWordPair);
            return ResponseEntity.ok(Map.of("message", "Word pair updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update word pair: " + e.getMessage());
        }
    }

    @Operation(summary = "단어쌍 삭제", description = "특정 용어집에서 단어쌍을 삭제합니다.")
    @DeleteMapping("/glossary/{id}/word-pair/{index}")
    public ResponseEntity<?> deleteWordPair(@PathVariable String id, @PathVariable int index) {
        try {
            translationService.deleteWordPair(id, index);
            return ResponseEntity.ok(Map.of("message", "Word pair deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete word pair: " + e.getMessage());
        }
    }

    @Operation(summary = "단어쌍 조회", description = "특정 용어집의 모든 단어쌍을 조회합니다.")
    @GetMapping("/glossary/{id}/word-pair")
    public ResponseEntity<?> getWordPairs(@PathVariable String id) {
        try {
            List<GlossaryRequest.WordPair> wordPairs = translationService.getWordPairs(id);

            // `_id`를 `id`로 변환
            List<Map<String, Object>> processedPairs = wordPairs.stream().map(pair -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", pair.getId()); // `_id`를 `id`로 매핑
                map.put("start", pair.getStart());
                map.put("arrival", pair.getArrival());
                return map;
            }).collect(Collectors.toList());
            log.info("Processed word pairs: {}", processedPairs);
            return ResponseEntity.ok(processedPairs);
        } catch (Exception e) {
            log.error("Failed to fetch word pairs for glossaryId {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching word pairs: " + e.getMessage());
        }
    }

}