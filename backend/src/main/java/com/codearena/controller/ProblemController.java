package com.codearena.controller;

import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.service.ProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long uid)
            return uid;
        return null;
    }

    private boolean currentIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority()))
                return true;
        }
        return false;
    }

    @GetMapping
    public List<Problem> list(@RequestParam(required = false) String topic,
            @RequestParam(required = false) String status) {
        boolean admin = currentIsAdmin();
        if (admin && "all".equalsIgnoreCase(status)) {
            return problemService.findAllForAdmin(topic);
        }
        if (admin && "pending".equalsIgnoreCase(status)) {
            return problemService.findPending();
        }
        return problemService.findApproved(topic);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending() {
        if (!currentIsAdmin())
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        return ResponseEntity.ok(problemService.findPending());
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine() {
        Long uid = currentUserId();
        if (uid == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(problemService.findMine(uid));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            Problem p = problemService.findViewable(id, currentUserId(), currentIsAdmin());
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Problem body) {
        Long uid = currentUserId();
        if (uid == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            Problem saved = problemService.createProblem(body, uid, currentIsAdmin());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        if (!currentIsAdmin())
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        try {
            return ResponseEntity.ok(problemService.approve(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        if (!currentIsAdmin())
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        try {
            return ResponseEntity.ok(problemService.reject(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/samples")
    public List<TestCase> samples(@PathVariable Long id) {
        return problemService.sampleTestCases(id);
    }

    @GetMapping("/{id}/testcases")
    public ResponseEntity<?> testCases(@PathVariable Long id) {
        Long uid = currentUserId();
        boolean admin = currentIsAdmin();
        Problem p;
        try {
            p = problemService.findById(id);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
        boolean isOwner = uid != null && uid.equals(p.getCreatedBy());
        if (!admin && !isOwner) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        return ResponseEntity.ok(problemService.allTestCases(id));
    }

    public static class TestCasePayload {
        public String input;
        public String expectedOutput;
        public Boolean isSample;
    }

    @PostMapping("/{id}/testcases")
    public ResponseEntity<?> createTestCase(@PathVariable Long id, @RequestBody TestCasePayload body) {
        try {
            problemService.assertCanEditTestCases(id, currentUserId(), currentIsAdmin());
            TestCase tc = problemService.createTestCase(id, body.input, body.expectedOutput, body.isSample);
            return ResponseEntity.ok(tc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/testcases/{tcId}")
    public ResponseEntity<?> updateTestCase(@PathVariable Long tcId, @RequestBody TestCasePayload body) {
        try {
            TestCase existing = problemService.findTestCaseById(tcId);
            problemService.assertCanEditTestCases(existing.getProblemId(), currentUserId(), currentIsAdmin());
            TestCase tc = problemService.updateTestCase(tcId, body.input, body.expectedOutput, body.isSample);
            return ResponseEntity.ok(tc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/testcases/{tcId}")
    public ResponseEntity<?> deleteTestCase(@PathVariable Long tcId) {
        try {
            TestCase existing = problemService.findTestCaseById(tcId);
            problemService.assertCanEditTestCases(existing.getProblemId(), currentUserId(), currentIsAdmin());
            problemService.deleteTestCase(tcId);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
