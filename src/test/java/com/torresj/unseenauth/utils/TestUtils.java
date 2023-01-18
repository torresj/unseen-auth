package com.torresj.unseenauth.utils;

import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;

import java.time.LocalDateTime;

public class TestUtils {
  public static UserEntity GenerateUser(
      String email, String password, Role role, AuthProvider provider, boolean validated) {
    return new UserEntity(
        1l,
        LocalDateTime.now(),
        LocalDateTime.now(),
        email,
        password,
        role,
        LocalDateTime.now(),
        email,
        null,
        1,
        validated,
        provider,
        123456789);
  }
}
