package com.smartrecruit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.service.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Security and validation checks: authentication, validation, lockout and token handling. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LlmClient llm;

    private void register(String json, int expected) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(expected));
    }

    @Test
    void healthAndJobBoardArePublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/api/jobs")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsRejectMissingOrForgedTokens() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationIsValidated() throws Exception {
        register("{\"name\":\"\",\"email\":\"a@b.com\",\"password\":\"Passw0rd!123\",\"role\":\"CANDIDATE\"}", 400);
        register("{\"name\":\"Bad Email\",\"email\":\"not-an-email\",\"password\":\"Passw0rd!123\",\"role\":\"CANDIDATE\"}", 400);
        register("{\"name\":\"Short Pw\",\"email\":\"short@example.com\",\"password\":\"123\",\"role\":\"CANDIDATE\"}", 400);
        register("{not json", 400);
    }

    @Test
    void nobodyCanRegisterAsAdministrator() throws Exception {
        register("{\"name\":\"Sneaky\",\"email\":\"sneaky@example.com\",\"password\":\"Passw0rd!123\",\"role\":\"ADMIN\"}", 400);
    }

    @Test
    void passwordsAreNeverReturned() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pat Private\",\"email\":\"pat@example.com\",\"password\":\"Passw0rd!123\",\"role\":\"CANDIDATE\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Passw0rd"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("passwordHash"))));
    }

    @Test
    void repeatedBadLoginsLockTheAccountTemporarily() throws Exception {
        register("{\"name\":\"Lock Me\",\"email\":\"lock@example.com\",\"password\":\"Passw0rd!123\",\"role\":\"CANDIDATE\"}", 201);
        String bad = "{\"email\":\"lock@example.com\",\"password\":\"wrong-password\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(bad))
                    .andExpect(status().isUnauthorized());
        }
        // Even the correct password is refused while locked.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"lock@example.com\",\"password\":\"Passw0rd!123\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
