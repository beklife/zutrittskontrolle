package de.musterstadt.zutritt.demo;

import de.musterstadt.zutritt.player.Player;
import de.musterstadt.zutritt.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Fictional demo data. Startup seeding and the nightly reset use the same method: {@link #resetDemoData()}.
 * Birth dates are relative to "today", so the demo cases (underage, turns 18 soon) stay valid every day.
 *
 * <p>Safety switch: this bean only exists with app.demo.enabled=true, because the reset deletes ALL data.
 * A real (non-demo) installation must never enable it.
 */
@Service
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);

    private final PlayerRepository playerRepository;
    private final JdbcClient jdbcClient;
    private final Clock clock;

    public DemoDataService(PlayerRepository playerRepository, JdbcClient jdbcClient, Clock clock) {
        this.playerRepository = playerRepository;
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedIfEmpty() {
        if (playerRepository.count() == 0) {
            resetDemoData();
        }
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Berlin")
    public void nightlyReset() {
        resetDemoData();
    }

    /** One transaction: either everything is reset or nothing. RESTART IDENTITY -> IDs start at 1 again. */
    @Transactional
    public void resetDemoData() {
        jdbcClient.sql("TRUNCATE TABLE access_attempt, player RESTART IDENTITY").update();
        playerRepository.saveAll(demoPlayers());
        log.info("Demo-Daten wurden zurückgesetzt.");
    }

    private List<Player> demoPlayers() {
        LocalDate today = LocalDate.now(clock);
        Instant now = clock.instant();

        Player blocked = new Player("Tom", "Testmann", today.minusYears(29).minusDays(40), now);
        blocked.setBlocked(true);

        return List.of(
                new Player("Erika", "Mustermann", today.minusYears(35).minusDays(120), now),
                new Player("Max", "Beispiel", today.minusYears(42).minusDays(10), now),
                new Player("Lena", "Probe", today.minusYears(16).minusDays(200), now),       // underage
                blocked,                                                                       // blocked
                new Player("Mia", "Muster", today.minusYears(18).plusDays(3), now),          // turns 18 in 3 days
                new Player("Jonas", "Demo", today.minusYears(67).minusDays(55), now));
    }
}
