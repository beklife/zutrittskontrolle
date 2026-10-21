package de.musterstadt.zutritt.access.dto;

import de.musterstadt.zutritt.access.AccessAttempt;
import de.musterstadt.zutritt.access.AccessReason;

import java.time.Instant;

public record AccessAttemptResponse(Long id, Long playerId, String playerName, Instant attemptedAt,
                                    boolean granted, AccessReason reason) {

    public static AccessAttemptResponse from(AccessAttempt attempt) {
        return new AccessAttemptResponse(attempt.getId(), attempt.getPlayer().getId(),
                attempt.getPlayer().getFullName(), attempt.getAttemptedAt(), attempt.isGranted(),
                attempt.getReason());
    }
}
