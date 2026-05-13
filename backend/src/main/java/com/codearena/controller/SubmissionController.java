package com.codearena.controller;

import com.codearena.dto.SubmissionDtos.*;
import com.codearena.entity.Submission;
import com.codearena.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody RunRequest req) {
        try {
            return ResponseEntity.ok(submissionService.run(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody SubmitRequest req) {
        try {
            Long userId = currentUserId();
            return ResponseEntity.ok(submissionService.submit(req, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/submissions")
    public ResponseEntity<?> mySubmissions(@RequestParam(required = false) Long problemId) {
        Long userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<Submission> subs = problemId != null
                ? submissionService.findByUserAndProblem(userId, problemId)
                : submissionService.findByUser(userId);
        return ResponseEntity.ok(subs);
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<?> submissionDetail(@PathVariable Long id) {
        Long userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Submission sub = submissionService.findById(id);
        if (sub == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        }
        if (!userId.equals(sub.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        return ResponseEntity.ok(sub);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long uid)
            return uid;
        return null;
    }
}
