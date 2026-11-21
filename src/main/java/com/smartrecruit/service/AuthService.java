package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.Candidate;
import com.smartrecruit.model.Role;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.CandidateRepository;
import com.smartrecruit.repository.UserRepository;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final CandidateRepository candidates;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService attempts;

    public AuthService(UserRepository users, CandidateRepository candidates, PasswordEncoder encoder,
                       JwtService jwt, LoginAttemptService attempts) {
        this.users = users;
        this.candidates = candidates;
        this.encoder = encoder;
        this.jwt = jwt;
        this.attempts = attempts;
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
        users.save(user);

        if (user.getRole() == Role.CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(user);
            candidate.setUpdatedAt(Instant.now());
            candidates.save(candidate);
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
        return new AuthResponse(jwt.generateToken(user), UserDto.of(user));
    }

    public UserDto me(Long userId) {
        return users.findById(userId).map(UserDto::of)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found."));
    }
}
