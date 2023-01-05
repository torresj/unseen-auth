package com.torresj.unseenauth.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!test && !local")
public class QueryDatasourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.queries")
    public DataSourceProperties queriesDataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    public DataSource queriesDataSource(){
        return queriesDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
}
