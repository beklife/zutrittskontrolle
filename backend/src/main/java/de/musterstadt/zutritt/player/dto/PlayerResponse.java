package de.musterstadt.zutritt.player.dto;

import de.musterstadt.zutritt.player.Player;

import java.time.LocalDate;

public record PlayerResponse(Long id, String firstName, String lastName, LocalDate birthDate, boolean blocked) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(player.getId(), player.getFirstName(), player.getLastName(),
                player.getBirthDate(), player.isBlocked());
    }
}
