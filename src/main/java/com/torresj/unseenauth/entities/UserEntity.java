package com.torresj.unseenauth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;

  @Column(updatable = false)
  @CreationTimestamp
  private LocalDateTime createAt;

  @Column @UpdateTimestamp private LocalDateTime updateAt;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column private LocalDateTime lastConnection;

  @Column private String name;

  @Column private String photoUrl;

  @Column private long numLogins;

  @Column private boolean validated;

  @Column(nullable = false, updatable = false)
  private AuthProvider provider;

  @Column private long nonce;
}
