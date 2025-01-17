package com.tension.gorani.users.domain.entity;

import com.tension.gorani.companies.domain.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 유저 고유 ID

    @Column(nullable = false, length = 50)
    private String username;  // 유저 이름

    @Column(nullable = false, length = 100)
    private String email;  // 이메일

    @Column(nullable = false, length = 20)
    private String provider;  // 소셜 제공자

    @Column(name = "provider_id", nullable = false)
    private String providerId;  // 소셜 제공자의 유저 고유 ID

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;  // 소속 기업

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // 계정 활성화 여부

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // 생성일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();  // 수정일

}
