package com.torresj.unseenauth.services;

import com.torresj.unseen.entities.AuthProvider;
import com.torresj.unseen.entities.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
class JwtServiceTest {

  private final String secret = "SecretKeyToGenerateAJSONWebTokens";
  private final String expiration = "1000000";
  private final String prefix = "Bearer";
  private final String issuer = "unseen";
  private final String email = "test@test.com";
  private final String role_key = "role";
  private final String provider_key = "Provider";
  private final JwtService jwtService = new JwtService(secret, expiration, prefix, issuer);

  @Test
  @DisplayName("Generate JWT test")
  void generateJWT() {
    log.info("Generating JWT");
    String jwt = jwtService.generateJWT(email, AuthProvider.UNSEEN, Role.ADMIN);

    log.info("Checking JWT");
    var claims =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(jwt.replace(prefix, ""))
            .getBody();

    Assertions.assertNotNull(jwt);
    Assertions.assertEquals(email, claims.getSubject());
    Assertions.assertEquals(issuer, claims.getIssuer());
    Assertions.assertEquals(Role.ADMIN.toString(), claims.get(role_key));
  }

  @Test
  @DisplayName("Authorize JWT test")
  void authorizeJWT() {
    log.info("Generating JWT");
    String jwt =
        Jwts.builder()
            .setIssuedAt(new Date())
            .setIssuer(issuer)
            .setSubject(email)
            .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expiration)))
            .claim(role_key, Role.ADMIN.toString())
            .claim(provider_key, AuthProvider.UNSEEN.toString())
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();

    log.info("Authorizing JWT");
    var result = jwtService.validateJWT(jwt);

    log.info("Checking result");
    Assertions.assertNotNull(result);
    Assertions.assertEquals(email, result.email());
    Assertions.assertEquals(Role.ADMIN, result.role());
  }
}
