package com.tension.gorani.users.service;

import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional
    public Users saveOrUpdateUser(String providerId, String email, String username, String provider) {
        log.info("Processing user with providerId: {}, email: {}, username: {}, provider: {}", providerId, email, username, provider);
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
            log.info("New user saved: {}", user);
        } else {
            log.info("Existing user found: {}", user);
        }
        return user;
    }
}
