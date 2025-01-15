package com.tension.gorani.config;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ResponseMessage {

    private int httpStatusCode;
    private String message;
    private Map<String, Object> results;


    public ResponseMessage(HttpStatus httpStatus, String message, Map<String, Object> results) {
        this.httpStatusCode = httpStatus.value();
        this.message = message;
        this.results = results;
    }

    // 메시지만 포함된 생성자
    public ResponseMessage(HttpStatus httpStatus, String message) {
        this.httpStatusCode = httpStatus.value();
        this.message = message;
        this.results = new HashMap<>(); // 결과를 빈 맵으로 초기화
    }


}