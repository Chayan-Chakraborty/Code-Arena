package com.codearena.dto;

import java.util.List;

public class SubmissionDtos {

    public static class RunRequest {
        public Long problemId;
        public String code;
        public String language; // java
    }

    public static class SubmitRequest {
        public Long problemId;
        public String code;
        public String language; // java
    }

    public static class TestResult {
        public String input;
        public String expectedOutput;
        public String actualOutput;
        public boolean passed;
        public String status;
        public Double time;

        public TestResult(String input, String expectedOutput, String actualOutput,
                boolean passed, String status, Double time) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.actualOutput = actualOutput;
            this.passed = passed;
            this.status = status;
            this.time = time;
        }
    }

    public static class RunResponse {
        public List<TestResult> results;

        public RunResponse(List<TestResult> results) {
            this.results = results;
        }
    }

    public static class SubmitResponse {
        public String status; // Accepted, Wrong Answer, TLE, Runtime Error, Compilation Error
        public Integer passed;
        public Integer total;
        public Double executionTime;
        public Long submissionId;
        public List<TestResult> results;
    }
}
