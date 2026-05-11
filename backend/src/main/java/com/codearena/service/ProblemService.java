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

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemService(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    public List<Problem> findAll(String topic) {
        if (topic == null || topic.isBlank())
            return problemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return problemRepository.findByTopicIgnoreCase(topic).stream()
                .sorted(Comparator.comparing(Problem::getId))
                .toList();
    }

    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
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
}
