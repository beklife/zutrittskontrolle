package de.musterstadt.zutritt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /** Business time zone of the gaming hall. Used for "today" and for "attempts of a day". */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Berlin");

    /**
     * The only place that reads the real system time. Tests use Clock.fixed(...) instead.
     */
    @Bean
    public Clock clock() {
        return Clock.system(BUSINESS_ZONE);
    }
}
