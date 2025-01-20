package com.tension.gorani.translation.DTO;

import java.util.List;

public class GlossaryRequest {

    private String name;
    private Long userId; // 추가: MySQL users.id 등을 담을 필드
    private List<WordPair> words;

    // Getter & Setter
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
        private String start;
        private String arrival;

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
    }
}
