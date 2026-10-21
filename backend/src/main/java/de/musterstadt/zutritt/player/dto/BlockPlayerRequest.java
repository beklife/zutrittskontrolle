package de.musterstadt.zutritt.player.dto;

import jakarta.validation.constraints.NotNull;

/** Boolean (not boolean): a missing value must be a 400, not silently "false". */
public record BlockPlayerRequest(@NotNull(message = "blocked ist erforderlich.") Boolean blocked) {
}
