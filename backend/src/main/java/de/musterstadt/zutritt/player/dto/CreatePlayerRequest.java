package de.musterstadt.zutritt.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePlayerRequest(
        @NotBlank(message = "Vorname ist erforderlich.")
        @Size(max = 50, message = "Vorname darf höchstens 50 Zeichen haben.")
        String firstName,

        @NotBlank(message = "Nachname ist erforderlich.")
        @Size(max = 50, message = "Nachname darf höchstens 50 Zeichen haben.")
        String lastName,

        @NotNull(message = "Geburtsdatum ist erforderlich.")
        @Past(message = "Geburtsdatum muss in der Vergangenheit liegen.")
        LocalDate birthDate) {
}
