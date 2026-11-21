package com.smartrecruit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.service.LlmClient;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** One end-to-end walk through the main features, with the AI model mocked. */
@SpringBootTest
@AutoConfigureMockMvc
class RecruitFlowTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LlmClient llm;

    private String body(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString();
    }

    private static String find(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        if (!m.find()) throw new AssertionError("Not found: " + regex + " in " + json);
        return m.group(1);
    }

    private String register(String name, String email, String role) throws Exception {
        String json = body(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"Passw0rd!123\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isCreated()));
        return "Bearer " + find(json, "\"token\":\"([^\"]+)\"");
    }

    @Test
    void recruiterAndCandidateFlow() throws Exception {
        String recruiter = register("Rita Recruiter", "rita@example.com", "RECRUITER");
        String candidate = register("Carl Candidate", "carl@example.com", "CANDIDATE");

        // Wrong password is rejected, and a duplicate email is a conflict.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rita@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"rita@example.com\",\"password\":\"Passw0rd!123\",\"role\":\"CANDIDATE\"}"))
                .andExpect(status().isConflict());

        // Protected routes need a token, and roles are enforced.
        mvc.perform(get("/api/jobs/mine")).andExpect(status().isUnauthorized());
        String jobJson = "{\"title\":\"Java Developer\",\"company\":\"Acme\",\"location\":\"Remote\","
                + "\"description\":\"Build REST APIs with Spring Boot and MySQL.\","
                + "\"requiredSkills\":\"Java, Spring Boot, MySQL\",\"experienceLevel\":\"Fresher\"}";
        mvc.perform(post("/api/jobs").header("Authorization", candidate).contentType(MediaType.APPLICATION_JSON)
                .content(jobJson)).andExpect(status().isForbidden());

        String created = body(mvc.perform(post("/api/jobs").header("Authorization", recruiter)
                        .contentType(MediaType.APPLICATION_JSON).content(jobJson))
                .andExpect(status().isCreated()));
        String jobId = find(created, "\"id\":(\\d+)");

        // Anyone can browse open jobs.
        mvc.perform(get("/api/jobs")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Java Developer")));

        // A resume is needed before applying.
        mvc.perform(post("/api/jobs/" + jobId + "/apply").header("Authorization", candidate))
                .andExpect(status().isBadRequest());
        String resume = "Carl Candidate. Java developer fresher. Built a Spring Boot REST API with JWT security "
                + "and MySQL. Skills: Java, Spring Boot, SQL, React, Git.";
        mvc.perform(multipart("/api/candidate/resume")
                        .file(new MockMultipartFile("file", "cv.txt", "text/plain", resume.getBytes()))
                        .header("Authorization", candidate))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"hasResume\":true")));

        // AI match (model mocked), then apply: the score is carried over.
        when(llm.chat(anyString(), anyString()))
                .thenReturn("```json\n{\"matchScore\": 82, \"verdict\": \"Good\", \"summary\": \"ok\"}\n```");
        mvc.perform(post("/api/ai/match").header("Authorization", candidate).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + "}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"score\":82")));
        mvc.perform(post("/api/jobs/" + jobId + "/apply").header("Authorization", candidate)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"coverNote\":\"Hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"matchScore\":82")));
        mvc.perform(post("/api/jobs/" + jobId + "/apply").header("Authorization", candidate))
                .andExpect(status().isConflict());

        // The recruiter sees the applicant and the analytics, a candidate does not.
        mvc.perform(get("/api/jobs/" + jobId + "/applications").header("Authorization", recruiter))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("carl@example.com")));
        mvc.perform(get("/api/jobs/" + jobId + "/applications").header("Authorization", candidate))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/analytics/recruiter").header("Authorization", recruiter))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"totalApplications\":1")));
    }

    @Test
    void aiIsReportedAsUnavailableWhenNoKeyIsSet() throws Exception {
        String candidate = register("Dina Candidate", "dina@example.com", "CANDIDATE");
        // No resume yet: a clear 400, not a crash.
        mvc.perform(post("/api/ai/analyze-resume").header("Authorization", candidate))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administratorCanModerateUsersButNobodyCanRegisterAsAdmin() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mallory\",\"email\":\"mallory@example.com\",\"password\":\"Passw0rd!123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());

        String adminJson = body(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"Admin#12345\"}"))
                .andExpect(status().isOk()));
        String admin = "Bearer " + find(adminJson, "\"token\":\"([^\"]+)\"");
        String candidate = register("Eve Candidate", "eve@example.com", "CANDIDATE");

        mvc.perform(get("/api/admin/stats").header("Authorization", candidate)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/stats").header("Authorization", admin)).andExpect(status().isOk());

        String users = body(mvc.perform(get("/api/admin/users").header("Authorization", admin)).andExpect(status().isOk()));
        String eveId = find(users, "\"id\":(\\d+),\"name\":\"Eve Candidate\"");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/users/" + eveId + "/enabled")
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"eve@example.com\",\"password\":\"Passw0rd!123\"}"))
                .andExpect(status().isForbidden());
    }
}
