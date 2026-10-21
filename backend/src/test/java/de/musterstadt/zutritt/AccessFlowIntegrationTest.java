package de.musterstadt.zutritt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Whole application + real PostgreSQL (Testcontainers): HTTP -> controller -> service -> DB and back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPlayer_check_block_check_andSeeBothAttemptsInLog() throws Exception {
        String birthDate = LocalDate.now().minusYears(30).toString();
        String created = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"  Integra ","lastName":"Tionstest","birthDate":"%s"}
                                """.formatted(birthDate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Integra"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(created.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":" + id + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.reason").value("GRANTED"))
                .andExpect(jsonPath("$.playerName").value("Integra Tionstest"));

        mockMvc.perform(put("/api/players/" + id + "/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blocked\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true));

        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":" + id + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("BLOCKED"));

        mockMvc.perform(get("/api/access-attempts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.playerId == " + id + ")].reason", hasItem("GRANTED")))
                .andExpect(jsonPath("$[?(@.playerId == " + id + ")].reason", hasItem("BLOCKED")));
    }

    @Test
    void unknownPlayer_returns404ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/access-checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Spieler mit ID 999999 wurde nicht gefunden."));
    }
}
