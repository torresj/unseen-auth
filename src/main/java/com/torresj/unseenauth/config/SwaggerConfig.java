package com.torresj.unseenauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.AllArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class SwaggerConfig {

  private static final String UUID_KEY = "UUID";
  @Value("${info.app.version}")
  private final String version;

  @Bean
  public OpenAPI springOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Unseen Auth API")
                .description(
                    "Spring boot microservice to allow applications to authenticate and authorize users for Unseen")
                .version(version)
                .license(
                    new License()
                        .name("GNU General Public License V3.0")
                        .url("https://www.gnu.org/licenses/gpl-3.0.html")));
  }

  @Bean
  public OperationCustomizer customize() {
    return (operation, handlerMethod) ->
        operation.addParametersItem(
            new Parameter()
                .in("header")
                .required(true)
                .description("UUID to identify this request in logs")
                .name(UUID_KEY));
  }
}
