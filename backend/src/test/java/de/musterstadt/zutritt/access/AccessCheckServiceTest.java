package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.access.dto.AccessCheckResponse;
import de.musterstadt.zutritt.common.PlayerNotFoundException;
import de.musterstadt.zutritt.config.AccessProperties;
import de.musterstadt.zutritt.config.ClockConfig;
import de.musterstadt.zutritt.player.Player;
import de.musterstadt.zutritt.player.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test: no Spring, no DB. Repositories are Mockito mocks, time is a fixed Clock.
 */
@ExtendWith(MockitoExtension.class)
class AccessCheckServiceTest {

    /** 22.10.2026, 12:00 in Berlin (10:00 UTC, summer time). */
    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AccessAttemptRepository accessAttemptRepository;

    private AccessCheckService service;

    @BeforeEach
    void setUp() {
        service = serviceAt(NOW);
    }

    private AccessCheckService serviceAt(Instant instant) {
        Clock clock = Clock.fixed(instant, ClockConfig.BUSINESS_ZONE);
        return new AccessCheckService(playerRepository, accessAttemptRepository, new AccessProperties(18), clock);
    }

    private void givenPlayer(long id, LocalDate birthDate, boolean blocked) {
        Player player = new Player("Erika", "Muster", birthDate, NOW);
        player.setBlocked(blocked);
        when(playerRepository.findById(id)).thenReturn(Optional.of(player));
        when(accessAttemptRepository.save(any(AccessAttempt.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void adult_isGranted() {
        givenPlayer(1, TODAY.minusYears(30), false);

        AccessCheckResponse result = service.check(1);

        assertThat(result.granted()).isTrue();
        assertThat(result.reason()).isEqualTo(AccessReason.GRANTED);
        assertThat(result.playerName()).isEqualTo("Erika Muster");
        assertThat(result.attemptedAt()).isEqualTo(NOW);
    }

    @Test
    void eighteenthBirthdayToday_isGranted() {
        givenPlayer(1, TODAY.minusYears(18), false);

        assertThat(service.check(1).reason()).isEqualTo(AccessReason.GRANTED);
    }

    @Test
    void eighteenthBirthdayTomorrow_isUnderage() {
        givenPlayer(1, TODAY.minusYears(18).plusDays(1), false);

        AccessCheckResponse result = service.check(1);

        assertThat(result.granted()).isFalse();
        assertThat(result.reason()).isEqualTo(AccessReason.UNDERAGE);
    }

    @Test
    void blockedAdult_isBlocked() {
        givenPlayer(1, TODAY.minusYears(30), true);

        AccessCheckResponse result = service.check(1);

        assertThat(result.granted()).isFalse();
        assertThat(result.reason()).isEqualTo(AccessReason.BLOCKED);
    }

    @Test
    void blockedAndUnderage_blockedWins() {
        givenPlayer(1, TODAY.minusYears(16), true);

        assertThat(service.check(1).reason()).isEqualTo(AccessReason.BLOCKED);
    }

    @Test
    void usesBerlinDate_notUtcDate() {
        // 21.10. 22:30 UTC is already 22.10. 00:30 in Berlin -> the player turned 18 today.
        service = serviceAt(Instant.parse("2026-10-21T22:30:00Z"));
        givenPlayer(1, LocalDate.of(2008, 10, 22), false);

        assertThat(service.check(1).reason()).isEqualTo(AccessReason.GRANTED);
    }

    @Test
    void bornOnLeapDay_becomesAdultOnFirstOfMarch() {
        givenPlayer(1, LocalDate.of(2008, 2, 29), false);

        service = serviceAt(Instant.parse("2026-02-28T12:00:00Z"));
        assertThat(service.check(1).reason()).isEqualTo(AccessReason.UNDERAGE);

        service = serviceAt(Instant.parse("2026-03-01T12:00:00Z"));
        assertThat(service.check(1).reason()).isEqualTo(AccessReason.GRANTED);
    }

    @Test
    void everyCheckIsSaved_withClockTime() {
        givenPlayer(1, TODAY.minusYears(16), false);

        service.check(1);

        ArgumentCaptor<AccessAttempt> saved = ArgumentCaptor.forClass(AccessAttempt.class);
        verify(accessAttemptRepository).save(saved.capture());
        assertThat(saved.getValue().getAttemptedAt()).isEqualTo(NOW);
        assertThat(saved.getValue().isGranted()).isFalse();
        assertThat(saved.getValue().getReason()).isEqualTo(AccessReason.UNDERAGE);
    }

    @Test
    void unknownPlayer_throwsAndSavesNothing() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.check(99))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("99");
        verify(accessAttemptRepository, never()).save(any());
    }
}
