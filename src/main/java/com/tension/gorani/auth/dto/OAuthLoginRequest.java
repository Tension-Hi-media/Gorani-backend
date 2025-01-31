package com.tension.gorani.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginRequest {
    private String code;
    private String state;
}
