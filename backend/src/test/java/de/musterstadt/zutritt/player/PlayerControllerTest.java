package de.musterstadt.zutritt.player;

import de.musterstadt.zutritt.common.PlayerNotFoundException;
import de.musterstadt.zutritt.player.dto.CreatePlayerRequest;
import de.musterstadt.zutritt.player.dto.PlayerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer only (controller + JSON + validation + exception handler). The service is a mock.
 */
@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void findAll_returnsPlayers() throws Exception {
        when(playerService.findAll()).thenReturn(List.of(
                new PlayerResponse(1L, "Erika", "Muster", LocalDate.of(1990, 5, 1), false)));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Erika"))
                .andExpect(jsonPath("$[0].birthDate").value("1990-05-01"))
                .andExpect(jsonPath("$[0].blocked").value(false));
    }

    @Test
    void create_validPlayer_returns201() throws Exception {
        when(playerService.create(any(CreatePlayerRequest.class))).thenReturn(
                new PlayerResponse(7L, "Max", "Beispiel", LocalDate.of(2000, 1, 1), false));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Max","lastName":"Beispiel","birthDate":"2000-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void create_invalidPlayer_returns400ProblemDetailWithFieldErrors() throws Exception {
        String tooLong = "x".repeat(51);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"%s","birthDate":"2999-01-01"}
                                """.formatted(tooLong)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validierungsfehler"))
                .andExpect(jsonPath("$.errors.firstName").value("Vorname ist erforderlich."))
                .andExpect(jsonPath("$.errors.lastName").value("Nachname darf höchstens 50 Zeichen haben."))
                .andExpect(jsonPath("$.errors.birthDate").value("Geburtsdatum muss in der Vergangenheit liegen."));

        verifyNoInteractions(playerService);
    }

    @Test
    void create_brokenJson_returns400() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":\"kein-datum\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void block_unknownPlayer_returns404() throws Exception {
        when(playerService.setBlocked(99L, true)).thenThrow(new PlayerNotFoundException(99L));

        mockMvc.perform(put("/api/players/99/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blocked\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Spieler nicht gefunden"));
    }

    @Test
    void block_missingValue_returns400() throws Exception {
        mockMvc.perform(put("/api/players/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.blocked").exists());
    }
}
