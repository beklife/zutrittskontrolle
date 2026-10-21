package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.access.dto.AccessCheckResponse;
import de.musterstadt.zutritt.common.PlayerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessController.class)
class AccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessCheckService accessCheckService;

    @MockitoBean
    private AccessAttemptService accessAttemptService;

    @Test
    void check_returns201WithResult() throws Exception {
        when(accessCheckService.check(1L)).thenReturn(new AccessCheckResponse(
                false, AccessReason.UNDERAGE, "Lena Probe", Instant.parse("2026-10-22T10:00:00Z")));

        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.granted").value(false))
                .andExpect(jsonPath("$.reason").value("UNDERAGE"))
                .andExpect(jsonPath("$.playerName").value("Lena Probe"))
                .andExpect(jsonPath("$.attemptedAt").value("2026-10-22T10:00:00Z"));
    }

    @Test
    void check_unknownPlayer_returns404() throws Exception {
        when(accessCheckService.check(99L)).thenThrow(new PlayerNotFoundException(99L));

        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void check_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.playerId").exists());
    }

    @Test
    void attempts_withDate_usesThatDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(accessAttemptService.findByDate(date)).thenReturn(List.of());

        mockMvc.perform(get("/api/access-attempts").param("date", "2026-10-01"))
                .andExpect(status().isOk());

        verify(accessAttemptService).findByDate(date);
    }

    @Test
    void attempts_invalidDate_returns400() throws Exception {
        mockMvc.perform(get("/api/access-attempts").param("date", "01.10.2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Ungültiger Wert für 'date'."));
    }
}
