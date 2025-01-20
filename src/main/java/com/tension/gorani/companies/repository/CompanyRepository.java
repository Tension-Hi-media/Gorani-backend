package com.tension.gorani.companies.repository;

import com.tension.gorani.companies.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findById(Long companyId);  // companyId로 회사 찾기
}
