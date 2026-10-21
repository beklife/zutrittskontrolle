package de.musterstadt.zutritt.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed config for "app.access.*" from application.yml.
 */
@Validated
@ConfigurationProperties(prefix = "app.access")
public record AccessProperties(@Min(0) int minimumAge) {
}
