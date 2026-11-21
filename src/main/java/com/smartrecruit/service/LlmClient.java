package com.smartrecruit.service;

import com.smartrecruit.exception.ApiException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Calls any OpenAI-compatible chat API (Gemini, Groq, OpenAI) from the Spring Boot backend. */
@Component
public class LlmClient {

    private final RestClient client;
    private final String apiKey;
    private final String model;

    public LlmClient(@Value("${app.ai.base-url}") String baseUrl,
                     @Value("${app.ai.api-key:}") String apiKey,
                     @Value("${app.ai.model}") String model,
                     @Value("${app.ai.timeout-seconds:60}") int timeoutSeconds) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.client = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .requestFactory(factory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
    }

    public boolean configured() {
        return !apiKey.isEmpty();
    }

    public String chat(String system, String user) {
        if (!configured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI is not configured on the server. Set the AI_API_KEY setting.");
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)));
        for (int attempt = 1; ; attempt++) {
            try {
                Map<?, ?> res = client.post().uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON).body(body)
                        .retrieve().body(Map.class);
                return extract(res);
            } catch (RestClientResponseException e) {
                int code = e.getStatusCode().value();
                if ((code == 429 || code == 503) && attempt == 1) {
                    sleep(2000);
                    continue;
                }
                if (code == 429) {
                    throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "The AI service is busy. Wait a moment and try again.");
                }
                if (code == 401 || code == 403) {
                    throw new ApiException(HttpStatus.BAD_GATEWAY, "The AI service rejected the server's API key.");
                }
                throw new ApiException(HttpStatus.BAD_GATEWAY, "The AI service returned an error (" + code + ").");
            } catch (ResourceAccessException e) {
                throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "The AI service took too long. Please try again.");
            }
        }
    }

    private static String extract(Map<?, ?> res) {
        try {
            List<?> choices = (List<?>) res.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String content = String.valueOf(message.get("content"));
            if (content.isBlank() || "null".equals(content)) throw new IllegalStateException();
            return content;
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "The AI service sent an empty answer. Try again.");
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
