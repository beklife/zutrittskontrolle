package de.musterstadt.zutritt.access.dto;

import de.musterstadt.zutritt.access.AccessReason;

import java.time.Instant;

public record AccessCheckResponse(boolean granted, AccessReason reason, String playerName, Instant attemptedAt) {
}
