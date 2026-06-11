package com.marcus.eventhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcus.eventhub.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class EventHubFlowIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullEventFlow() throws Exception {
        String ownerToken = registerAndLogin("owner-flow@test.com", "Owner Flow");
        String guestToken = registerAndLogin("guest-flow@test.com", "Guest Flow");

        String eventId = createEvent(ownerToken);

        mockMvc.perform(post("/events/{eventId}/registrations", eventId)
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/events/{eventId}/participants", eventId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("guest-flow@test.com"));

        mockMvc.perform(get("/events/registered")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId));
    }

    @Test
    void protectedRouteWithoutTokenShouldReturnUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void ownerOnlyUpdateShouldForbidOtherUsers() throws Exception {
        String ownerToken = registerAndLogin("owner-only@test.com", "Owner Only");
        String otherToken = registerAndLogin("intruder@test.com", "Intruder");
        String eventId = createEvent(ownerToken);

        Instant start = Instant.now().plusSeconds(86_400);
        Instant end = start.plusSeconds(7_200);
        String body = """
                {
                  "title": "Hacked",
                  "description": "No",
                  "location": "SP",
                  "startDateTime": "%s",
                  "endDateTime": "%s",
                  "maxParticipants": 10
                }
                """.formatted(start, end);

        mockMvc.perform(put("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only the event owner can perform this action"));
    }

    @Test
    void refreshTokenFlowShouldIssueNewAccessToken() throws Exception {
        register("refresh@test.com", "Refresh User");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"refresh@test.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginJson.get("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void softDeletedEventShouldNotAppearInListings() throws Exception {
        String ownerToken = registerAndLogin("soft-delete@test.com", "Soft Delete");

        String eventId = createEvent(ownerToken);

        mockMvc.perform(delete("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/events")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/events/{id}", eventId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void eventListShouldSupportPaginationAndTitleFilter() throws Exception {
        String token = registerAndLogin("filter@test.com", "Filter User");

        createEvent(token, "Java Meetup", "SP");
        createEvent(token, "Python Meetup", "RJ");

        mockMvc.perform(get("/events")
                        .param("title", "Java")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Java Meetup"));
    }

    private void register(String email, String name) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"123456"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin(String email, String name) throws Exception {
        register(email, name);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"123456"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertThat(json.get("token").asText()).isNotBlank();
        return json.get("token").asText();
    }

    private String createEvent(String token) throws Exception {
        return createEvent(token, "Integration Meetup", "SP");
    }

    private String createEvent(String token, String title, String location) throws Exception {
        Instant start = Instant.now().plusSeconds(86_400);
        Instant end = start.plusSeconds(7_200);
        String body = """
                {
                  "title": "%s",
                  "description": "Test",
                  "location": "%s",
                  "startDateTime": "%s",
                  "endDateTime": "%s",
                  "maxParticipants": 10
                }
                """.formatted(title, location, start, end);

        MvcResult result = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }
}
