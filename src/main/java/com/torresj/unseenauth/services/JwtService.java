package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    public JwtService(@Value("${jwt.token.secret}") String secret, @Value("${jwt.token.expiration}")
    String expiration, @Value("${jwt.token.prefix}") String prefix, @Value("${jwt.token.issuer.info}")  String issuer){
        this.secret = secret;
        this.expiration = expiration;
        this.prefix = prefix;
        this.issuer = issuer;
    }
    private final String secret;
    private final String expiration;
    private final String prefix;
    private final String issuer;
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
