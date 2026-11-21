package com.smartrecruit.service;

import com.smartrecruit.exception.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Blocks an email for 15 minutes after 5 failed logins (in memory, fine for one server). */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public void checkNotBlocked(String email) {
        Deque<Instant> list = failures.get(key(email));
        if (list == null) return;
        synchronized (list) {
            prune(list);
            if (list.size() >= MAX_FAILURES) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many failed attempts. Try again in 15 minutes.");
            }
        }
    }

    public void recordFailure(String email) {
        Deque<Instant> list = failures.computeIfAbsent(key(email), k -> new ArrayDeque<>());
        synchronized (list) {
            prune(list);
            list.addLast(Instant.now());
        }
    }

    public void clear(String email) {
        failures.remove(key(email));
    }

    private static void prune(Deque<Instant> list) {
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!list.isEmpty() && list.peekFirst().isBefore(cutoff)) list.removeFirst();
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
