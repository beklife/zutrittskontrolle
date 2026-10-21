package de.musterstadt.zutritt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on @Scheduled methods (nightly demo reset). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
