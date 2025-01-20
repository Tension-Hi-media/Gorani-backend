package com.tension.gorani.companies.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class CreateCompanyDTO {

    private String name;  // 기업 이름
    private String registrationNumber;  // 사업자 등록번호
    private LocalDateTime createdAt = LocalDateTime.now();  // 생성일
    private LocalDateTime updatedAt = LocalDateTime.now();  // 수정일

}
