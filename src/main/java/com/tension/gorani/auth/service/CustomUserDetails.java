package com.tension.gorani.auth.service;

import com.tension.gorani.users.domain.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CustomUserDetails implements UserDetails {

    private Users users;

    public String getEmail() {
        return users.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한을 반환합니다. 여기서는 ROLE_USER로 설정합니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return null; // OAuth를 통해 로그인하므로 패스워드는 필요 없습니다.
    }

    @Override
    public String getUsername() {
        return users.getUsername(); // 사용자 이름 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }

    // 사용자 정보를 반환하는 메서드 추가
    public Users getUserInfo() {
        return users; // Users 객체 반환
    }

    // CustomUserDetails에서 회사 정보 반환 메서드 개선
    public String getCompanyName() {
        return users.getCompany() != null ? users.getCompany().getName() : "입력되지않음";
    }

    public String getCompanyRepresentative() {
        return users.getCompany() != null ? users.getCompany().getRepresentativeName() : "입력되지않음";
    }

    public String getCompanyRegistrationNumber() {
        return users.getCompany() != null ? users.getCompany().getRegistrationNumber() : "입력되지않음";
    }

    public Long getCompanyId() {
        return users.getCompany() != null ? users.getCompany().getCompanyId() : null;
    }

}