package com.tension.gorani.companies.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tension.gorani.users.domain.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;  // 기업 고유 ID

    @Column(nullable = false, length = 100)
    private String name;  // 기업 이름

    @Column(name = "registration_number", unique = true, length = 20)
    private String registrationNumber;  // 사업자 등록번호

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "representative_name")
    private String representativeName;    // 대표 이름

    @JsonBackReference // ✅ 유저 리스트에서 무한 참조 방지
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Users> users = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
