package com.torresj.unseenauth.dtos;

import com.torresj.unseen.entities.AuthProvider;

public record AuthSocialTokenDTO(String token, AuthProvider provider, long nonce) {}
