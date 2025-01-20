package com.tension.gorani.users.domain.dto;

import com.tension.gorani.companies.domain.entity.Company;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class UpdateCompanyResponse {

    private Long id;  // 유저 고유 ID
    private String provider;  // 소셜 제공자
    private String username;  // 유저 이름
    private String email;  // 이메일
    private String providerId;  // 소셜 제공자의 유저 고유 ID
    private Company company;  // 소속 기업
    private LocalDateTime createdAt;  // 생성일

}
