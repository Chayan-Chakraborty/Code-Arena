package com.codearena.service;

import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.TestCaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProblemService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemService(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    /** Public listing — only APPROVED problems. */
    public List<Problem> findApproved(String topic) {
        List<Problem> list = (topic == null || topic.isBlank())
                ? problemRepository.findByStatus(STATUS_APPROVED)
                : problemRepository.findByStatusAndTopicIgnoreCase(STATUS_APPROVED, topic);
        return list.stream().sorted(Comparator.comparing(Problem::getId)).toList();
    }

    /** Admin listing — all problems (optionally filtered by topic). */
    public List<Problem> findAllForAdmin(String topic) {
        if (topic == null || topic.isBlank())
            return problemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return problemRepository.findByTopicIgnoreCase(topic).stream()
                .sorted(Comparator.comparing(Problem::getId))
                .toList();
    }

    public List<Problem> findPending() {
        return problemRepository.findByStatus(STATUS_PENDING).stream()
                .sorted(Comparator.comparing(Problem::getId))
                .toList();
    }

    public List<Problem> findMine(Long userId) {
        if (userId == null)
            return List.of();
        return problemRepository.findByCreatedByOrderByIdDesc(userId);
    }

    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    /**
     * Return the problem if the requester is allowed to view it. Admins and the
     * creator can see PENDING/REJECTED entries; everyone else only sees APPROVED.
     */
    public Problem findViewable(Long id, Long userId, boolean admin) {
        Problem p = findById(id);
        if (STATUS_APPROVED.equals(p.getStatus()))
            return p;
        if (admin)
            return p;
        if (userId != null && userId.equals(p.getCreatedBy()))
            return p;
        throw new RuntimeException("Problem not available");
    }

    public Problem createProblem(Problem payload, Long userId, boolean admin) {
        if (payload.getTitle() == null || payload.getTitle().isBlank()) {
            throw new RuntimeException("Title is required");
        }
        Problem p = Problem.builder()
                .title(payload.getTitle().trim())
                .description(payload.getDescription())
                .difficulty(payload.getDifficulty())
                .topic(payload.getTopic())
                .constraints(payload.getConstraints())
                .sampleInput(payload.getSampleInput())
                .sampleOutput(payload.getSampleOutput())
                .starterCode(payload.getStarterCode())
                .status(admin ? STATUS_APPROVED : STATUS_PENDING)
                .createdBy(userId)
                .build();
        return problemRepository.save(p);
    }

    public Problem approve(Long id) {
        Problem p = findById(id);
        p.setStatus(STATUS_APPROVED);
        return problemRepository.save(p);
    }

    public Problem reject(Long id) {
        Problem p = findById(id);
        p.setStatus(STATUS_REJECTED);
        return problemRepository.save(p);
    }

    public List<TestCase> sampleTestCases(Long problemId) {
        return testCaseRepository.findByProblemIdAndIsSample(problemId, true);
    }

    public List<TestCase> hiddenTestCases(Long problemId) {
        return testCaseRepository.findByProblemIdAndIsSample(problemId, false);
    }

    public List<TestCase> allTestCases(Long problemId) {
        return testCaseRepository.findByProblemId(problemId);
    }

    /**
     * Permission rule for test-case CRUD:
     * - Admins can always edit.
     * - The problem creator can edit while the problem is PENDING.
     */
    public void assertCanEditTestCases(Long problemId, Long userId, boolean admin) {
        Problem p = findById(problemId);
        if (admin)
            return;
        if (userId != null && userId.equals(p.getCreatedBy()) && STATUS_PENDING.equals(p.getStatus()))
            return;
        throw new RuntimeException("Not allowed to modify test cases for this problem");
    }

    public TestCase findTestCaseById(Long testCaseId) {
        return testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found: " + testCaseId));
    }

    public TestCase createTestCase(Long problemId, String input, String expectedOutput, Boolean isSample) {
        findById(problemId); // ensure exists
        TestCase tc = TestCase.builder()
                .problemId(problemId)
                .input(input == null ? "" : input)
                .expectedOutput(expectedOutput == null ? "" : expectedOutput)
                .isSample(isSample != null && isSample)
                .build();
        return testCaseRepository.save(tc);
    }

    public TestCase updateTestCase(Long testCaseId, String input, String expectedOutput, Boolean isSample) {
        TestCase tc = findTestCaseById(testCaseId);
        if (input != null)
            tc.setInput(input);
        if (expectedOutput != null)
            tc.setExpectedOutput(expectedOutput);
        if (isSample != null)
            tc.setIsSample(isSample);
        return testCaseRepository.save(tc);
    }

    public void deleteTestCase(Long testCaseId) {
        if (!testCaseRepository.existsById(testCaseId)) {
            throw new RuntimeException("Test case not found: " + testCaseId);
        }
        testCaseRepository.deleteById(testCaseId);
    }
}
