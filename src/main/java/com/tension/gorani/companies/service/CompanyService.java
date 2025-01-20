package com.tension.gorani.companies.service;

import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // Create
    public Company createCompany(Company company) {
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

