package de.musterstadt.zutritt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI: http://localhost:8080/swagger-ui.html (disabled in profile "prod"). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Zutrittskontrolle API")
                .description("Demo mit erfundenen Daten: Spieler verwalten und Zutritt prüfen.")
                .version("1.0"));
    }
}
