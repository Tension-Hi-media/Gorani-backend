package com.tension.gorani.users.domain.dto;

import com.tension.gorani.companies.domain.dto.CreateCompanyDTO;
import com.tension.gorani.users.domain.entity.Users;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String provider;
    private String providerId;
    private CreateCompanyDTO company;

    public static UserResponseDTO from(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .company(user.getCompany() != null ? CreateCompanyDTO.from(user.getCompany()) : null)
                .build();
    }
}
