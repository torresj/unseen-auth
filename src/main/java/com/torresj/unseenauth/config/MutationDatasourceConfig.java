package com.torresj.unseenauth.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!test && !local")
public class MutationDatasourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource.mutations")
  public DataSourceProperties mutationsDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  public DataSource mutationsDataSource() {
    return mutationsDataSourceProperties().initializeDataSourceBuilder().build();
  }
}
