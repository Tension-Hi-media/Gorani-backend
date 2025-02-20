package com.tension.gorani.companies.controller;

import com.tension.gorani.companies.domain.dto.CreateCompanyDTO;
import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.service.CompanyService;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;
    private final UserService userService;

    // ✅ 기업 정보 저장
    @PostMapping("/register")
    public ResponseEntity<CreateCompanyDTO> createCompany(@RequestBody CreateCompanyDTO createCompanyDTO) {
        log.info("📢 기업 등록 요청: {}", createCompanyDTO);

        if (createCompanyDTO.getName() == null || createCompanyDTO.getRegistrationNumber() == null || createCompanyDTO.getRepresentativeName() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Company savedCompany = companyService.createCompany(createCompanyDTO);
        log.info("✅ 기업 등록 완료: {}", savedCompany);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateCompanyDTO.from(savedCompany));
    }

    // ✅ 모든 기업 정보 조회
    @GetMapping
    public ResponseEntity<List<CreateCompanyDTO>> getAllCompanies() {
        log.info("📢 모든 기업 정보 조회 요청");

        List<CreateCompanyDTO> companies = companyService.getAllCompanies()
                .stream()
                .map(CreateCompanyDTO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(companies);
    }

    // ✅ 특정 기업 정보 조회 (companyId 기준)
    @GetMapping("/{companyId}")
    public ResponseEntity<CreateCompanyDTO> getCompanyById(@PathVariable Long companyId) {
        log.info("📢 기업 ID 조회 요청: companyId={}", companyId);

        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 기업을 찾을 수 없습니다. companyId=" + companyId));

        return ResponseEntity.ok(CreateCompanyDTO.from(company));
    }

    // ✅ 특정 유저의 회사 정보 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<CreateCompanyDTO> getCompanyByUserId(@PathVariable Long userId) {
        log.info("📢 유저 ID로 기업 정보 조회 요청: userId={}", userId);

        Users user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 유저를 찾을 수 없습니다. userId=" + userId));

        if (user.getCompany() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(CreateCompanyDTO.from(user.getCompany()));
    }

    // ✅ 기업 정보 수정
    @PutMapping("/{companyId}")
    public ResponseEntity<CreateCompanyDTO> updateCompany(@PathVariable Long companyId, @RequestBody CreateCompanyDTO updatedCompanyDTO) {
        log.info("📢 기업 정보 수정 요청: companyId={}, data={}", companyId, updatedCompanyDTO);

        Company updatedCompany = Company.builder()
                .name(updatedCompanyDTO.getName())
                .registrationNumber(updatedCompanyDTO.getRegistrationNumber())
                .updatedAt(updatedCompanyDTO.getUpdatedAt())
                .representativeName(updatedCompanyDTO.getRepresentativeName())
                .build();

        Company savedCompany = companyService.updateCompany(companyId, updatedCompany);
        return ResponseEntity.ok(CreateCompanyDTO.from(savedCompany));
    }

    // ✅ 기업 정보 삭제
    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long companyId) {
        log.info("📢 기업 삭제 요청: companyId={}", companyId);
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok("✅ 기업 삭제 완료");
    }
}
