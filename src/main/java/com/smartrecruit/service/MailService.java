package com.smartrecruit.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends transactional email through Brevo's HTTPS API (port 443).
 * SMTP ports are blocked on Render's free tier, so the API is used instead of SMTP.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final URI ENDPOINT = URI.create("https://api.brevo.com/v3/smtp/email");

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public MailService(@Value("${app.mail.brevo-api-key:}") String apiKey,
                       @Value("${app.mail.sender-email:}") String senderEmail,
                       @Value("${app.mail.sender-name:SmartRecruit}") String senderName) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.senderEmail = senderEmail == null ? "" : senderEmail.trim();
        this.senderName = senderName == null || senderName.isBlank() ? "SmartRecruit" : senderName.trim();
    }

    public boolean isEnabled() {
        return !apiKey.isEmpty() && !senderEmail.isEmpty();
    }

    /** Sends in the background so a slow mail provider never blocks the request. Failures are only logged. */
    public void sendAsync(String toEmail, String toName, String subject, String htmlContent) {
        if (!isEnabled()) return;
        CompletableFuture.runAsync(() -> {
            try {
                String body = "{\"sender\":{\"name\":" + q(senderName) + ",\"email\":" + q(senderEmail) + "},"
                        + "\"to\":[{\"email\":" + q(toEmail) + ",\"name\":" + q(toName) + "}],"
                        + "\"subject\":" + q(subject) + ",\"htmlContent\":" + q(htmlContent) + "}";
                HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                        .timeout(Duration.ofSeconds(20))
                        .header("api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    String text = response.body() == null ? "" : response.body();
                    log.error("Brevo rejected the email (HTTP {}): {}", response.statusCode(),
                            text.length() > 300 ? text.substring(0, 300) : text);
                }
            } catch (Exception e) {
                log.error("Could not send email: {}", e.toString());
            }
        });
    }

    /** Minimal JSON string encoder. */
    private static String q(String value) {
        String s = value == null ? "" : value;
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
