package com.tension.gorani.companies.controller;

import com.tension.gorani.companies.domain.dto.CreateCompanyDTO;
import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;

    // ✅ 기업 정보 저장
    @PostMapping
    public ResponseEntity<?> createCompany(@RequestBody CreateCompanyDTO createCompanyDTO) {
        log.info("API call to create a company: {}", createCompanyDTO);

        // 필수 입력값 검증
        if (createCompanyDTO.getName() == null || createCompanyDTO.getRegistrationNumber() == null || createCompanyDTO.getRepresentativeName() == null) {
            return ResponseEntity.badRequest().body("기업명, 사업자 등록번호, 대표자명은 필수 입력값입니다.");
        }

        Company savedCompany = companyService.createCompany(createCompanyDTO);
        return ResponseEntity.ok(savedCompany);
    }

    // ✅ 모든 기업 정보 가져오기
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    // ✅ 특정 기업 정보 가져오기 (companyId 기준)
    @GetMapping("/{companyId}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long companyId) {
        log.info("Fetching company info for ID: {}", companyId);

        Optional<Company> company = companyService.getCompanyById(companyId);

        if (company.isPresent()) {
            return ResponseEntity.ok(company.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 기업 정보를 찾을 수 없습니다.");
        }
    }
}
