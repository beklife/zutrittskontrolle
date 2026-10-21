package de.musterstadt.zutritt.demo;

import de.musterstadt.zutritt.TestcontainersConfiguration;
import de.musterstadt.zutritt.access.AccessCheckService;
import de.musterstadt.zutritt.access.AccessReason;
import de.musterstadt.zutritt.player.Player;
import de.musterstadt.zutritt.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DemoDataServiceTest {

    @Autowired
    private DemoDataService demoDataService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AccessCheckService accessCheckService;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void reset_createsSixPlayers_withExpectedDemoCases_andClearsLog() {
        demoDataService.resetDemoData();
        accessCheckService.check(1);

        demoDataService.resetDemoData();

        List<Player> players = playerRepository.findAll();
        assertThat(players).hasSize(6);
        assertThat(players).extracting(Player::getId).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(jdbcClient.sql("select count(*) from access_attempt").query(Long.class).single()).isZero();

        assertThat(reasonFor(players, "Lena")).isEqualTo(AccessReason.UNDERAGE);
        assertThat(reasonFor(players, "Mia")).isEqualTo(AccessReason.UNDERAGE);
        assertThat(reasonFor(players, "Tom")).isEqualTo(AccessReason.BLOCKED);
        assertThat(reasonFor(players, "Erika")).isEqualTo(AccessReason.GRANTED);
    }

    private AccessReason reasonFor(List<Player> players, String firstName) {
        Player player = players.stream().filter(p -> p.getFirstName().equals(firstName)).findFirst().orElseThrow();
        return accessCheckService.check(player.getId()).reason();
    }
}
