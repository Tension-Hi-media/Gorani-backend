package com.tension.gorani.companies.domain.dto;

import com.tension.gorani.companies.domain.entity.Company;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class CreateCompanyDTO {

    private Long companyId;
    private String name;
    private String registrationNumber;
    private String representativeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Entity → DTO 변환 메서드
    public static CreateCompanyDTO from(Company company) {
        return CreateCompanyDTO.builder()
                .companyId(company.getCompanyId())
                .name(company.getName())
                .registrationNumber(company.getRegistrationNumber())
                .representativeName(company.getRepresentativeName())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

}
