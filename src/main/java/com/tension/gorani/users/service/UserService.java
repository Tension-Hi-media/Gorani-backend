package com.tension.gorani.users.service;

import com.tension.gorani.companies.domain.entity.Company;
import com.tension.gorani.companies.repository.CompanyRepository;
import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsersRepository usersRepository;
    private final CompanyRepository companyRepository;

    // ✅ 유저 저장 또는 업데이트 (소셜 로그인 시 사용)
    @Transactional
    public Users saveOrUpdateUser(String providerId, String email, String username, String provider) {
        log.info("📢 유저 저장 또는 업데이트 요청: providerId={}, email={}, username={}, provider={}", providerId, email, username, provider);

        Users user = usersRepository.findByProviderId(providerId);
        if (user == null) {
            user = Users.builder()
                    .providerId(providerId)
                    .email(email)
                    .username(username)
                    .provider(provider)
                    .isActive(true)
                    .build();
            usersRepository.save(user);
            log.info("✅ 신규 유저 저장 완료: {}", user);
        } else {
            log.info("✅ 기존 유저 확인: {}", user);
        }

        return user;
    }

    // ✅ 유저의 기업 정보 업데이트 (기업 등록 후 유저 정보 업데이트)
    @Transactional
    public Users updateUserWithCompany(Long userId, Long companyId) {
        log.info("📢 유저의 기업 정보 업데이트 요청: userId={}, companyId={}", userId, companyId);

        Users foundUser = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 유저를 찾을 수 없습니다. userId=" + userId));

        Company foundCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 회사를 찾을 수 없습니다. companyId=" + companyId));

        foundUser.setCompany(foundCompany);
        log.info("✅ 유저의 회사 정보 업데이트 완료: {}", foundUser);

        return usersRepository.save(foundUser);
    }

    // ✅ 특정 유저 정보 조회
    public Optional<Users> getUserById(Long id) {
        return usersRepository.findById(id);
    }

}
