package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.access.dto.AccessCheckResponse;
import de.musterstadt.zutritt.common.PlayerNotFoundException;
import de.musterstadt.zutritt.config.AccessProperties;
import de.musterstadt.zutritt.player.Player;
import de.musterstadt.zutritt.player.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

/**
 * Decides if a player may enter and logs every attempt.
 *
 * <p>Rule order: BLOCKED is checked before UNDERAGE. If a player is blocked AND underage,
 * the reason is BLOCKED (a block is an explicit staff decision and does not change on the 18th birthday).
 */
@Service
public class AccessCheckService {

    private final PlayerRepository playerRepository;
    private final AccessAttemptRepository accessAttemptRepository;
    private final AccessProperties accessProperties;
    private final Clock clock;

    public AccessCheckService(PlayerRepository playerRepository,
                              AccessAttemptRepository accessAttemptRepository,
                              AccessProperties accessProperties,
                              Clock clock) {
        this.playerRepository = playerRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.accessProperties = accessProperties;
        this.clock = clock;
    }

    @Transactional
    public AccessCheckResponse check(long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        Instant now = clock.instant();
        LocalDate today = LocalDate.ofInstant(now, clock.getZone());
        AccessReason reason = decide(player, today);

        AccessAttempt attempt = accessAttemptRepository.save(new AccessAttempt(player, now, reason));
        return new AccessCheckResponse(attempt.isGranted(), attempt.getReason(), player.getFullName(),
                attempt.getAttemptedAt());
    }

    private AccessReason decide(Player player, LocalDate today) {
        if (player.isBlocked()) {
            return AccessReason.BLOCKED;
        }
        if (ageOn(player.getBirthDate(), today) < accessProperties.minimumAge()) {
            return AccessReason.UNDERAGE;
        }
        return AccessReason.GRANTED;
    }

    /** Full years. Period handles leap years: born 29 Feb -> one year older on 1 March in non-leap years. */
    private static int ageOn(LocalDate birthDate, LocalDate day) {
        return Period.between(birthDate, day).getYears();
    }
}
