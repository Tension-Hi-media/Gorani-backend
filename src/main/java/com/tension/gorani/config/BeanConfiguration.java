package com.tension.gorani.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BeanConfiguration {
    @Bean
    public ModelMapper modelMapper() {

        // entity와 dto 간의 변환을 용이하게 해주는 라이브러리

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);

        return new ModelMapper();
    }

    // Java 8에 도입된 새로운 날짜 및 시간 API(LocalDate, LocalTime, LocalDateTime)를 직렬화 가능토록 함
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 빈 객체 오류 무시
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}