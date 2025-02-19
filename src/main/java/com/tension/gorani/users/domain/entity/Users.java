package com.tension.gorani.users.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", initialValue = 200, allocationSize = 1)
    private Long id;  // 유저 고유 ID

    @Column(nullable = false, length = 50)
    private String username;  // 유저 이름

    @Column(nullable = false, length = 100)
    private String email;  // 이메일

    @Column(nullable = false, length = 20)
    private String provider;  // 소셜 제공자

    @Column(name = "provider_id", nullable = false, unique = true)
    private String providerId;  // 소셜 제공자의 유저 고유 ID

    @JsonBackReference  // ✅ 순환 참조 방지
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;  // 소속 기업

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // 계정 활성화 여부

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // 생성일

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 수정일
}
