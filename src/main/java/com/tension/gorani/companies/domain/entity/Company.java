    package com.tension.gorani.companies.domain.entity;

    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @Entity
    @Table(name = "Companies")
    public class Company {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long companyId;  // 기업 고유 ID

        @Column(nullable = false, length = 100)
        private String name;  // 기업 이름

        @Column(name = "registration_number", unique = true, length = 50)
        private String registrationNumber;  // 사업자 등록번호

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();  // 생성일

        @Column(name = "updated_at")
        private LocalDateTime updatedAt = LocalDateTime.now();  // 수정일

        @Column(name = "ceo_name", nullable = false)
        private String ceoName;  // 대표자명

    }
