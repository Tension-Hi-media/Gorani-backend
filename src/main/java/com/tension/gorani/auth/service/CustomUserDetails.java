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
        // 회원의 권한을 GrantedAuthority 로 변환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // oAuth를 통해 로그인하므로 패스워드는 없기 때문에 null로 반환했다.
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return users.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부를 직접 관리하고 싶다면
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
        return true; // 활성화 여부
    }
}