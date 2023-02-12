package com.torresj.unseenauth.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

@Configuration
@Profile("!test && !local")
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.torresj.unseen.repositories.queries",
    entityManagerFactoryRef = "queriesEntityManagerFactory",
    transactionManagerRef = "queriesTransactionManager")
public class QueryJpaConfig {
  @Bean
  public LocalContainerEntityManagerFactoryBean queriesEntityManagerFactory(
      @Qualifier("queriesDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
    return builder
        .dataSource(dataSource)
        .packages("com.torresj.unseen.entities")
        .properties(jpaProperties())
        .build();
  }

  @Bean
  public PlatformTransactionManager queriesTransactionManager(
      @Qualifier("queriesEntityManagerFactory")
          LocalContainerEntityManagerFactoryBean queriesEntityManagerFactory) {
    return new JpaTransactionManager(
        Objects.requireNonNull(queriesEntityManagerFactory.getObject()));
  }

  protected Map<String, ?> jpaProperties() {
    return Map.of(
        "hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName(),
        "hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
  }
}
