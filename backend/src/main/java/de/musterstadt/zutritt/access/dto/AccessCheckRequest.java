package de.musterstadt.zutritt.access.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AccessCheckRequest(
        @NotNull(message = "playerId ist erforderlich.")
        @Positive(message = "playerId muss positiv sein.")
        Long playerId) {
}
