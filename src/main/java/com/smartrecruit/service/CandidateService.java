package com.smartrecruit.service;

import com.smartrecruit.dto.Dtos.*;
import com.smartrecruit.exception.ApiException;
import com.smartrecruit.model.Candidate;
import com.smartrecruit.repository.CandidateRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidateService {

    private static final int MAX_TEXT = 30_000;
    private final CandidateRepository candidates;

    public CandidateService(CandidateRepository candidates) {
        this.candidates = candidates;
    }

    public Candidate forUser(Long userId) {
        return candidates.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Only candidates have a profile."));
    }

    @Transactional(readOnly = true)
    public CandidateDto profile(Long userId) {
        return CandidateDto.of(forUser(userId));
    }

    @Transactional
    public CandidateDto update(Long userId, ProfileRequest req) {
        Candidate c = forUser(userId);
        c.setPhone(blankToNull(req.phone()));
        c.setHeadline(blankToNull(req.headline()));
        c.setSkills(blankToNull(req.skills()));
        c.setExperienceYears(req.experienceYears());
        c.setUpdatedAt(Instant.now());
        return CandidateDto.of(candidates.save(c));
    }

    /** Reads the text from a PDF or TXT resume. The file itself is not kept, only its text. */
    @Transactional
    public CandidateDto uploadResume(Long userId, MultipartFile file) {
        Candidate c = forUser(userId);
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a resume file to upload.");
        }
        String name = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String lower = name.toLowerCase();
        String text;
        try {
            byte[] bytes = file.getBytes();
            if (lower.endsWith(".pdf")) {
                if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F') {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "That file is not a valid PDF.");
                }
                try (PDDocument doc = Loader.loadPDF(bytes)) {
                    text = new PDFTextStripper().getText(doc);
                }
            } else if (lower.endsWith(".txt")) {
                text = new String(bytes, StandardCharsets.UTF_8);
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Upload a PDF or TXT resume.");
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not read that file. Try another PDF.");
        }
        text = text.replace("\u0000", "").replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
        if (text.length() < 50) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No readable text found. Scanned image PDFs are not supported; use a text-based PDF.");
        }
        if (text.length() > MAX_TEXT) text = text.substring(0, MAX_TEXT);
        c.setResumeText(text);
        c.setResumeFileName(name.length() > 250 ? name.substring(0, 250) : name);
        c.setUpdatedAt(Instant.now());
        return CandidateDto.of(candidates.save(c));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
