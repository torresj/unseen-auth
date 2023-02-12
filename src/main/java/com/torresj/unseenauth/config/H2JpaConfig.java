package com.torresj.unseenauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("test || local")
@EnableJpaRepositories(
    basePackages = {
      "com.torresj.unseen.repositories.mutations",
      "com.torresj.unseen.repositories.queries"
    })
public class H2JpaConfig {}
