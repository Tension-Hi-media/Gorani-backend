package com.tension.gorani.companies.controller;

import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Company", description = "기업 정보 관리 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;

    // Create
    @Operation(summary = "기업 생성", description = "새로운 기업 정보를 생성합니다.")
    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        log.info("Creating company: {}", company.getName()); // 로그 나중에 삭제
        Company createdCompany = companyService.createCompany(company);
        return ResponseEntity.ok(createdCompany);
    }

    // Read - All
    @Operation(summary = "기업 목록 조회", description = "등록된 모든 기업 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        log.info("Fetching all companies"); // 로그 나중에 삭제
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    // Read - By ID
    @Operation(summary = "기업 조회", description = "기업 ID로 특정 기업 정보를 조회합니다.")
    @GetMapping("/{companyId}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long companyId) {
        log.info("Fetching company with ID: {}", companyId); // 로그 나중에 삭제
        return companyService.getCompanyById(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @Operation(summary = "기업 수정", description = "기업 ID를 기반으로 기업 정보를 수정합니다.")
    @PutMapping("/{companyId}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long companyId, @RequestBody Company updatedCompany) {
        log.info("Updating company with ID: {}", companyId); // 로그 나중에 삭제
        try {
            Company company = companyService.updateCompany(companyId, updatedCompany);
            return ResponseEntity.ok(company);
        } catch (IllegalArgumentException e) {
            log.error("Error updating company with ID: {}", companyId, e); // 로그 나중에 삭제
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @Operation(summary = "기업 삭제", description = "기업 ID를 기반으로 기업 정보를 삭제합니다.")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        log.info("Deleting company with ID: {}", companyId); // 로그 나중에 삭제
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
