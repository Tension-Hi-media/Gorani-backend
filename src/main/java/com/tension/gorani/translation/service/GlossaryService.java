package com.tension.gorani.translation.service;

import com.tension.gorani.translation.DTO.GlossaryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlossaryService {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    private final RestTemplate restTemplate;

    // [1] 용어집 생성 (FastAPI 호출 후 결과 반환)
    public Map<String, Object> saveGlossary(GlossaryRequest glossaryRequest) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/api/glossary",
                    glossaryRequest,
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("FastAPI 응답 에러: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error while saving glossary: {}", e.getMessage(), e);
            throw new RuntimeException("Error while saving glossary", e);
        }
    }

    // [2] 용어집 이름 변경
    public void updateGlossaryName(String id, String name) {
        try {
            String url = fastApiUrl + "/api/glossary/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, String> requestBody = Map.of("name", name);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    // [3] 특정 유저의 용어집 목록 가져오기
    public List<Map<String, Object>> fetchUserGlossaries(int userId) {
        try {
            String url = fastApiUrl + "/glossary?userId=" + userId;
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

    // [4] 용어집 삭제
    public Map<String, Object> deleteGlossary(String glossaryId) {
        try {
            // DELETE 요청 후, FastAPI가 {"message":"용어집 삭제 성공"} 같은 걸 반환하도록 만듦
            String url = fastApiUrl + "/api/glossary/" + glossaryId;
            // RestTemplate의 exchange 메서드 사용 (deleteForEntity도 가능)
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, null, Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody(); // 예: {"message":"용어집 삭제 성공"}
            } else {
                throw new RuntimeException("FastAPI 응답 에러: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting glossary: " + e.getMessage(), e);
        }
    }

// Spring에서 FastAPI 응답을 처리하는 부분
    public String setDefaultGlossary(String userId, String glossaryId) {
        try {
            // 1. FastAPI에서 기본 용어집 설정 전에 기존 용어집을 갱신하는 로직을 추가합니다.
            // FastAPI URL
            String url = UriComponentsBuilder.fromHttpUrl(fastApiUrl + "/api/v1/glossary/" + userId + "/default")
                    .queryParam("glossary_id", glossaryId)
                    .toUriString();

            // 2. 기존 기본 용어집을 false로 설정하기 위해 API를 먼저 호출
            String resetDefaultGlossaryUrl = fastApiUrl + "/api/v1/glossary/" + userId + "/reset-default";  // 새로운 API 경로 추가 (가정)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> resetResponse = restTemplate.exchange(resetDefaultGlossaryUrl, HttpMethod.PUT, requestEntity, String.class);

            if (!resetResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("기존 용어집을 초기화하는 FastAPI 요청 실패: " + resetResponse.getBody());
            }

            // 3. FastAPI를 호출하여 기본 용어집 설정
            HttpEntity<?> setDefaultEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, setDefaultEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("FastAPI 요청 실패: " + response.getBody());
            }

            // 4. 응답에서 'glossary'만 추출하여, 필요한 포맷으로 변환
            String responseBody = response.getBody();
            if (responseBody != null && responseBody.contains("glossary")) {
                responseBody = responseBody.replaceAll("\"_id\": \"([a-f0-9]{24})\"", "\"id\": \"$1\"");

                // 5. 변경된 용어집 리스트 반환
                return responseBody;  // FastAPI에서 응답 받은 업데이트된 용어집 리스트 반환
            } else {
                throw new RuntimeException("응답에서 기본 용어집을 찾을 수 없습니다.");
            }

        } catch (HttpClientErrorException e) {
            log.error("FastAPI 요청 오류: 상태 코드 = {}, 본문 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("FastAPI 요청 중 클라이언트 오류 발생", e);
        } catch (Exception e) {
            log.error("FastAPI 요청 중 알 수 없는 오류 발생", e);
            throw new RuntimeException("FastAPI 요청 중 오류 발생", e);
        }
    }

    // [4] 단어쌍 추가
    public void addWordPair(String glossaryId, GlossaryRequest.WordPair wordPair) {
        try {
            log.info("Adding word pair to glossaryId: {}", glossaryId);
            restTemplate.postForObject(fastApiUrl + "/api/glossary/" + glossaryId + "/word-pair", wordPair, Void.class);
        } catch (Exception e) {
            log.error("Error while adding word pair: {}", e.getMessage(), e);
            throw new RuntimeException("Error while adding word pair: " + e.getMessage());
        }
    }

    // [5] 단어쌍 수정
    public void updateWordPair(String glossaryId, String wordPairId, GlossaryRequest.WordPair updatedWordPair) {
        try {
            log.info("Updating word pair: glossaryId={}, wordPairId={}", glossaryId, wordPairId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<GlossaryRequest.WordPair> requestEntity = new HttpEntity<>(updatedWordPair, headers);
            String url = String.format("%s/api/glossary/%s/word-pair/%s", fastApiUrl, glossaryId, wordPairId);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            log.info("Response from FastAPI: {}", response.getBody());
            log.info("Word pair updated successfully. glossaryId={}, wordPairId={}", glossaryId, wordPairId);
        } catch (Exception e) {
            log.error("Error while updating word pair: glossaryId={}, wordPairId={}", glossaryId, wordPairId, e);
            throw new RuntimeException("Error while updating word pair", e);
        }
    }

    // [6] 단어쌍 삭제
    public void deleteWordPair(String glossaryId, int index) {
        try {
            log.info("Deleting word pair from glossaryId: {}, index: {}", glossaryId, index);
            restTemplate.delete(fastApiUrl + "/api/glossary/" + glossaryId + "/word-pair/" + index);
        } catch (Exception e) {
            log.error("Error while deleting word pair: {}", e.getMessage(), e);
            throw new RuntimeException("Error while deleting word pair: " + e.getMessage());
        }
    }

    // [7] 단어쌍 목록 가져오기
    public List<GlossaryRequest.WordPair> getWordPairs(String glossaryId) {
        try {
            log.info("Fetching word pairs for glossaryId: {}", glossaryId);

            ResponseEntity<GlossaryRequest.WordPair[]> response = restTemplate.getForEntity(
                    fastApiUrl + "/api/glossary/" + glossaryId + "/word-pair",
                    GlossaryRequest.WordPair[].class
            );

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                throw new RuntimeException("FastAPI 응답이 비어 있습니다.");
            }
        } catch (Exception e) {
            log.error("Error while fetching word pairs for glossaryId {}: {}", glossaryId, e.getMessage());
            throw new RuntimeException("FastAPI 호출 중 오류", e);
        }
    }
}
