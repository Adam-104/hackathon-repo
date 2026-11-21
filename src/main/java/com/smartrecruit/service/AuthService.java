package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.Candidate;
import com.smartrecruit.model.Role;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.CandidateRepository;
import com.smartrecruit.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
public class AuthService {

    private final UserRepository users;
    private final CandidateRepository candidates;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService attempts;
    private final MailService mail;
    private final String frontendUrl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private final Map<String, Instant> lastResend = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, CandidateRepository candidates, PasswordEncoder encoder,
                       JwtService jwt, LoginAttemptService attempts, MailService mail,
                       @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.users = users;
        this.candidates = candidates;
        this.encoder = encoder;
        this.jwt = jwt;
        this.attempts = attempts;
        this.mail = mail;
        this.frontendUrl = frontendUrl == null ? "" : frontendUrl.trim().replaceAll("/+$", "");
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose Candidate or HR / Recruiter.");
        }
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(req.role());
        user.setCreatedAt(Instant.now());
        boolean needsVerification = mail.isEnabled();
        user.setEmailVerified(!needsVerification);
        String rawToken = needsVerification ? prepareVerification(user) : null;
        users.save(user);

        if (user.getRole() == Role.CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(user);
            candidate.setUpdatedAt(Instant.now());
            candidates.save(candidate);
        }
        if (needsVerification) {
            sendVerificationEmail(user, rawToken);
            // No token yet: the account becomes usable after the email link is opened.
            return new AuthResponse(null, UserDto.of(user));
        }
        return new AuthResponse(jwt.generateToken(user), UserDto.of(user));
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        attempts.checkNotBlocked(email);
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPasswordHash())) {
            attempts.recordFailure(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password.");
        }
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been disabled by an administrator.");
        }
        attempts.clear(email);
        if (!user.isEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Please verify your email first. Check your inbox or request a new link.");
        }
        return new AuthResponse(jwt.generateToken(user), UserDto.of(user));
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = users.findByVerificationTokenHash(sha256(token.trim())).orElse(null);
        if (user == null || user.getVerificationExpiresAt() == null
                || user.getVerificationExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This verification link is invalid or has expired.");
        }
        user.setEmailVerified(true);
        user.setVerificationTokenHash(null);
        user.setVerificationExpiresAt(null);
        users.save(user);
        return new MessageResponse("Email verified. You can sign in now.");
    }

    /** Always answers the same way, so it cannot be used to find out which emails have accounts. */
    @Transactional
    public MessageResponse resendVerification(String rawEmail) {
        MessageResponse generic = new MessageResponse("If that account needs verification, a new link is on its way.");
        String email = rawEmail.trim().toLowerCase();
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || user.isEmailVerified() || !mail.isEnabled()) return generic;
        Instant last = lastResend.get(email);
        if (last != null && last.plus(RESEND_COOLDOWN).isAfter(Instant.now())) return generic;
        lastResend.put(email, Instant.now());
        String rawToken = prepareVerification(user);
        users.save(user);
        sendVerificationEmail(user, rawToken);
        return generic;
    }

    /** Stores only a hash of the token; the raw token exists only in the email link. */
    private String prepareVerification(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        user.setVerificationTokenHash(sha256(rawToken));
        user.setVerificationExpiresAt(Instant.now().plus(TOKEN_LIFETIME));
        return rawToken;
    }

    private void sendVerificationEmail(User user, String rawToken) {
        String link = frontendUrl + "/verify-email?token=" + rawToken;
        String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#1d2226\">"
                + "<h2 style=\"color:#0a66c2\">Confirm your email</h2>"
                + "<p>Hi " + HtmlUtils.htmlEscape(user.getName()) + ",</p>"
                + "<p>Welcome to SmartRecruit. Please confirm your email address to activate your account.</p>"
                + "<p><a href=\"" + link + "\" style=\"display:inline-block;background:#0a66c2;color:#fff;"
                + "padding:12px 24px;border-radius:24px;text-decoration:none;font-weight:bold\">Verify email</a></p>"
                + "<p style=\"color:#666;font-size:13px\">This link expires in 24 hours. "
                + "If you did not create this account, you can ignore this email.</p></div>";
        mail.sendAsync(user.getEmail(), user.getName(), "Confirm your SmartRecruit email", html);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public UserDto me(Long userId) {
        return users.findById(userId).map(UserDto::of)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found."));
    }
}
