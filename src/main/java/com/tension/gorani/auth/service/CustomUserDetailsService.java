package com.tension.gorani.auth.service;

import com.tension.gorani.users.domain.entity.Users;
import com.tension.gorani.users.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository; // 사용자 정보를 가져오는 레포지토리

    @Override
    public UserDetails loadUserByUsername(String memberNo) throws UsernameNotFoundException {

        try {
            // memberNo를 long 타입으로 변환
            long id = Long.parseLong(memberNo);

            // memberRepository에서 회원 조회
            Users member = usersRepository.findById(id)
                    .orElseThrow(() -> new AuthenticationServiceException("가입되지 않은 회원입니다."));

            return new CustomUserDetails(member);
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("회원 번호 형식이 올바르지 않습니다.", e);
        }
    }

}