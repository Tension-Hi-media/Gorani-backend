package com.tension.gorani.translation.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.util.List;

public class GlossaryRequest {
    @JsonProperty("_id") // JSON의 '_id'를 Java의 'id'로 매핑
    @Id // MongoDB의 _id와 연결
    private String id;

    private String name; // 용어집 이름
    private Long userId; // 사용자 ID
    private List<WordPair> words; // 단어쌍 리스트


    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<WordPair> getWords() {
        return words;
    }

    public void setWords(List<WordPair> words) {
        this.words = words;
    }

    // Inner class for WordPair
    public static class WordPair {
        @JsonProperty("_id") // JSON의 '_id'를 Java의 'id'로 매핑
        private String id; // 개별 단어쌍의 고유 ID
        private String start;
        private String arrival;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getArrival() {
            return arrival;
        }

        public void setArrival(String arrival) {
            this.arrival = arrival;
        }

        @Override
        public String toString() {
            return "WordPair{" +
                    "id='" + id + '\'' +
                    ", start='" + start + '\'' +
                    ", arrival='" + arrival + '\'' +
                    '}';
        }
    }
}