package de.musterstadt.zutritt.common;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(long playerId) {
        super("Spieler mit ID " + playerId + " wurde nicht gefunden.");
    }
}
