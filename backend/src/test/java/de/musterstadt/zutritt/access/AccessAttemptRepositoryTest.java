package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.TestcontainersConfiguration;
import de.musterstadt.zutritt.player.Player;
import de.musterstadt.zutritt.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against a real PostgreSQL (Testcontainers) with the real Flyway schema.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccessAttemptRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AccessAttemptRepository accessAttemptRepository;

    @Test
    void findInRange_returnsOnlyAttemptsInsideRange_newestFirst() {
        Player player = playerRepository.save(
                new Player("Erika", "Beispiel", LocalDate.of(1990, 5, 1), Instant.parse("2026-01-01T00:00:00Z")));

        Instant from = Instant.parse("2026-03-10T00:00:00Z");
        Instant to = Instant.parse("2026-03-11T00:00:00Z");

        accessAttemptRepository.save(new AccessAttempt(player, from.minusSeconds(1), AccessReason.GRANTED));
        AccessAttempt first = accessAttemptRepository.save(new AccessAttempt(player, from, AccessReason.GRANTED));
        AccessAttempt second = accessAttemptRepository.save(
                new AccessAttempt(player, from.plusSeconds(3600), AccessReason.BLOCKED));
        accessAttemptRepository.save(new AccessAttempt(player, to, AccessReason.GRANTED));

        List<AccessAttempt> result = accessAttemptRepository.findInRange(from, to);

        assertThat(result).extracting(AccessAttempt::getId).containsExactly(second.getId(), first.getId());
        assertThat(result.getFirst().isGranted()).isFalse();
        assertThat(result.getFirst().getReason()).isEqualTo(AccessReason.BLOCKED);
    }

    @Test
    void playersAreSortedByLastNameThenFirstName() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        playerRepository.save(new Player("Zoe", "Muster", LocalDate.of(1990, 1, 1), now));
        playerRepository.save(new Player("Anna", "Muster", LocalDate.of(1990, 1, 1), now));
        playerRepository.save(new Player("Max", "Beispiel", LocalDate.of(1990, 1, 1), now));

        assertThat(playerRepository.findAllByOrderByLastNameAscFirstNameAsc())
                .extracting(Player::getFullName)
                .containsExactly("Max Beispiel", "Anna Muster", "Zoe Muster");
    }
}
