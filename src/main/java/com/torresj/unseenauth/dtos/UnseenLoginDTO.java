package com.torresj.unseenauth.dtos;

public record UnseenLoginDTO(String email, String password, long nonce) {}
