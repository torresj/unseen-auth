package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expiration}")
    private String expiration;

    @Value("${jwt.token.prefix}")
    private String prefix;
    @Value("${jwt.token.header}")
    private String StringHeader;

    @Value("${jwt.token.issuer.info}")
    private String issuer;

    private String ROLE_KEY="role";

    private final String PROVIDER_KEY = "Provider";

    public String generateJWT(String email, AuthProvider provider, Role role){
        log.debug("[JWT SERVICE] Generating JWT");
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setIssuer(issuer)
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis()+ Long.parseLong(expiration)))
                .claim(ROLE_KEY, role.name())
                .claim(PROVIDER_KEY, provider.name())
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public AuthorizeResponseDTO validateJWT(String jwt){
        var claims = Jwts
                .parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(jwt.replace(prefix, ""))
                .getBody();
        String email = claims.getSubject();
        String role = (String) claims.get(ROLE_KEY);
        return new AuthorizeResponseDTO(email, Role.valueOf(role));
    }
}
