package com.codearena.service;

import com.codearena.dto.SubmissionDtos.*;
import com.codearena.entity.Submission;
import com.codearena.entity.TestCase;
import com.codearena.repository.SubmissionRepository;
import com.codearena.service.LocalJavaExecutor.ExecutionResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionService {

    private final ProblemService problemService;
    private final LocalJavaExecutor executor;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(ProblemService problemService, LocalJavaExecutor executor,
            SubmissionRepository submissionRepository) {
        this.problemService = problemService;
        this.executor = executor;
        this.submissionRepository = submissionRepository;
    }

    public RunResponse run(RunRequest req) {
        List<TestCase> samples = problemService.sampleTestCases(req.problemId);
        List<TestResult> results = new ArrayList<>();
        for (TestCase tc : samples) {
            results.add(executeOne(req.code, tc));
        }
        return new RunResponse(results);
    }

    public SubmitResponse submit(SubmitRequest req, Long userId) {
        List<TestCase> hidden = problemService.hiddenTestCases(req.problemId);
        List<TestResult> results = new ArrayList<>();
        String overallStatus = "Accepted";
        int passed = 0;
        double maxTime = 0;

        for (TestCase tc : hidden) {
            TestResult r = executeOne(req.code, tc);
            results.add(r);
            if (r.time != null && r.time > maxTime)
                maxTime = r.time;
            if (r.passed) {
                passed++;
            } else if ("Accepted".equals(overallStatus)) {
                overallStatus = r.status;
            }
        }
        if (hidden.isEmpty())
            overallStatus = "No Test Cases";

        Submission sub = Submission.builder()
                .userId(userId)
                .problemId(req.problemId)
                .code(req.code)
                .status(overallStatus)
                .executionTime(maxTime)
                .build();
        sub = submissionRepository.save(sub);

        SubmitResponse resp = new SubmitResponse();
        resp.status = overallStatus;
        resp.passed = passed;
        resp.total = hidden.size();
        resp.executionTime = maxTime;
        resp.submissionId = sub.getId();
        resp.results = results;
        return resp;
    }

    public List<Submission> findByProblem(Long problemId) {
        return submissionRepository.findByProblemIdOrderByCreatedAtDesc(problemId);
    }

    public List<Submission> findByUser(Long userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Submission> findByUserAndProblem(Long userId, Long problemId) {
        return submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId);
    }

    public Submission findById(Long id) {
        return submissionRepository.findById(id).orElse(null);
    }

    private TestResult executeOne(String code, TestCase tc) {
        ExecutionResult ex = executor.execute(code, tc.getInput());
        String actual;
        if ("Accepted".equals(ex.status)) {
            actual = ex.stdout == null ? "" : ex.stdout;
        } else {
            actual = ex.error != null && !ex.error.isBlank() ? ex.error
                    : (ex.stdout == null ? "" : ex.stdout);
        }
        boolean passed = "Accepted".equals(ex.status)
                && normalize(actual).equals(normalize(tc.getExpectedOutput()));
        String status = passed ? "Passed" : ("Accepted".equals(ex.status) ? "Wrong Answer" : ex.status);
        return new TestResult(tc.getInput(), tc.getExpectedOutput(), actual, passed, status, ex.time);
    }

    private String normalize(String s) {
        if (s == null)
            return "";
        return s.replace("\r\n", "\n").trim();
    }
}
