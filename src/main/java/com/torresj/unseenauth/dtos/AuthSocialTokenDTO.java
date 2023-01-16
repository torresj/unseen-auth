package com.torresj.unseenauth.dtos;

import com.torresj.unseenauth.entities.AuthProvider;

public record AuthSocialTokenDTO(String token, AuthProvider provider, long nonce) {
}
