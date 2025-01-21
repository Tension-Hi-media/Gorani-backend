package com.tension.gorani.companies.service;

import com.tension.gorani.companies.domain.dto.CreateCompanyDTO;
import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.repository.CompanyRepository;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;

    // Create
    public Company createCompany(CreateCompanyDTO createCompanyDTO) {
        Company company = Company.builder()
                .name(createCompanyDTO.getName())
                .createdAt(createCompanyDTO.getCreatedAt())
                .updatedAt(createCompanyDTO.getUpdatedAt())
                .registrationNumber(createCompanyDTO.getRegistrationNumber())
                .representativeName(createCompanyDTO.getRepresentativeName())
                .build();
        return companyRepository.save(company);
    }

    // Read - All
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // Read - By ID
    public Optional<Company> getCompanyById(Long companyId) {
        return companyRepository.findById(companyId);
    }

    // Update
    public Company updateCompany(Long companyId, Company updatedCompany) {
        return companyRepository.findById(companyId)
                .map(existingCompany -> {
                    existingCompany.setName(updatedCompany.getName());
                    existingCompany.setRegistrationNumber(updatedCompany.getRegistrationNumber());
                    existingCompany.setUpdatedAt(updatedCompany.getUpdatedAt());
                    return companyRepository.save(existingCompany);
                })
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found."));
    }

    // Delete
    public void deleteCompany(Long companyId) {
        companyRepository.deleteById(companyId);
    }
}

