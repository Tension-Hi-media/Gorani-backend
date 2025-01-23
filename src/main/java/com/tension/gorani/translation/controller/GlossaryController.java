package com.tension.gorani.translation.controller;

import com.tension.gorani.translation.DTO.GlossaryRequest;
import com.tension.gorani.translation.service.GlossaryService;
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

@Tag(name = "Glossary")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/glossary")
public class GlossaryController {

    private final GlossaryService glossaryService;

    // [1] 용어집 생성
    @Operation(summary = "용어집 저장", description = "새로운 용어집을 저장합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveGlossary(@RequestBody GlossaryRequest glossaryRequest) {
        try {
            log.info("Saving glossary: {}", glossaryRequest);

            // FastAPI에서 생성된 용어집 데이터를 Map으로 반환받음
            Map<String, Object> savedGlossary = glossaryService.saveGlossary(glossaryRequest);

            log.info("FastAPI Response: {}", savedGlossary);

            // React로 생성된 용어집 데이터를 그대로 반환
            return ResponseEntity.ok(savedGlossary);
        } catch (Exception e) {
            log.error("Failed to save glossary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // [2] 용어집 이름 변경
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGlossaryName(
            @PathVariable String id,
            @RequestBody Map<String, String> requestBody) {
        try {
            String name = requestBody.get("name");
            glossaryService.updateGlossaryName(id, name);
            return ResponseEntity.ok(Map.of("message", "용어집 이름 업데이트 성공", "updatedName", name));
        } catch (Exception e) {
            log.error("Failed to update glossary name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업데이트 실패: " + e.getMessage());
        }
    }

    // [3] 용어집 조회
    @GetMapping
    public ResponseEntity<?> getGlossaries(@RequestParam int userId) {
        try {
            log.info("Fetching glossaries for userId: {}", userId);
            List<Map<String, Object>> glossaries = glossaryService.fetchUserGlossaries(userId);
            // FastAPI로부터 받은 List<Map<String,Object>> 그대로 React에게 반환
            return ResponseEntity.ok(glossaries);
        } catch (Exception e) {
            log.error("Failed to fetch glossaries for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // [4] 용어집 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGlossary(@PathVariable String id) {
        try {
            Map<String, Object> response = glossaryService.deleteGlossary(id);
            // 예: {"message":"용어집 삭제 성공"} 를 FastAPI가 보내주면, 여기서 그대로 반환
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to delete glossary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // [5] 용어집 기본 설정
    @Operation(summary = "기본 용어집 설정", description = "특정 사용자의 기본 용어집을 설정합니다.")
    @PutMapping("/{userId}/default")
    public ResponseEntity<?> setDefaultGlossary(
            @PathVariable String userId,
            @RequestBody Map<String, String> requestBody) {

        String glossaryId = requestBody.get("glossaryId");

        if (glossaryId == null || glossaryId.isEmpty()) {
            return ResponseEntity.badRequest().body("Glossary ID is missing");
        }

        try {
            // FastAPI 호출을 통해 기본 용어집 설정 및 모든 용어집 반환
            String updatedGlossariesJson = glossaryService.setDefaultGlossary(userId, glossaryId);

            // 리액트에 전달할 JSON 문자열 반환
            return ResponseEntity.ok(updatedGlossariesJson);
        } catch (Exception e) {
            log.error("기본 용어집 설정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("기본 용어집 설정 실패: " + e.getMessage());
        }
    }

    // [6] 단어쌍 추가
    @Operation(summary = "단어쌍 추가", description = "특정 용어집에 단어쌍을 추가합니다.")
    @PostMapping("/{id}/word-pair")
    public ResponseEntity<?> addWordPair(@PathVariable String id, @RequestBody GlossaryRequest.WordPair wordPair) {
        try {
            glossaryService.addWordPair(id, wordPair);
            return ResponseEntity.ok(Map.of("message", "Word pair added successfully"));
        } catch (Exception e) {
            log.error("Failed to add word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add word pair: " + e.getMessage());
        }
    }

    // [7] 단어쌍 수정
    @Operation(summary = "단어쌍 수정", description = "특정 용어집의 단어쌍을 수정합니다.")
    @PutMapping("/{glossaryId}/word-pair/{wordPairId}")
    public ResponseEntity<?> updateWordPair(
            @PathVariable String glossaryId,
            @PathVariable String wordPairId,
            @RequestBody GlossaryRequest.WordPair updatedWordPair) {
        try {
            glossaryService.updateWordPair(glossaryId, wordPairId, updatedWordPair);
            return ResponseEntity.ok(Map.of("message", "Word pair updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update word pair: " + e.getMessage());
        }
    }

    // [8] 단어쌍 삭제
    @Operation(summary = "단어쌍 삭제", description = "특정 용어집에서 단어쌍을 삭제합니다.")
    @DeleteMapping("/{id}/word-pair/{index}")
    public ResponseEntity<?> deleteWordPair(@PathVariable String id, @PathVariable int index) {
        try {
            glossaryService.deleteWordPair(id, index);
            return ResponseEntity.ok(Map.of("message", "Word pair deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete word pair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete word pair: " + e.getMessage());
        }
    }

    // [9] 단어쌍 조회
    @Operation(summary = "단어쌍 조회", description = "특정 용어집의 모든 단어쌍을 조회합니다.")
    @GetMapping("/{id}/word-pair")
    public ResponseEntity<?> getWordPairs(@PathVariable String id) {
        try {
            List<GlossaryRequest.WordPair> wordPairs = glossaryService.getWordPairs(id);
            // _id -> id 로 변환
            List<Map<String, Object>> processedPairs = wordPairs.stream().map(pair -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", pair.getId());
                map.put("start", pair.getStart());
                map.put("arrival", pair.getArrival());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(processedPairs);
        } catch (Exception e) {
            log.error("Failed to fetch word pairs for glossaryId {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching word pairs: " + e.getMessage());
        }
    }
}
