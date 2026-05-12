package com.codearena;

import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.TestCaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseSeedSmokeTest {

    private static final int EXPECTED_PROBLEMS = 33 * 40;
    private static final int EXPECTED_TEST_CASES_PER_PROBLEM = 5;
    private static final int EXPECTED_SAMPLE_TEST_CASES_PER_PROBLEM = 2;
    private static final int EXPECTED_HIDDEN_TEST_CASES_PER_PROBLEM = 3;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Test
    void initializesSchemaAndSeedsProblemTestCases() {
        assertThat(problemRepository.count()).isEqualTo(EXPECTED_PROBLEMS);
        assertThat(testCaseRepository.count()).isEqualTo(EXPECTED_PROBLEMS * EXPECTED_TEST_CASES_PER_PROBLEM);

        Problem problem = problemRepository.findAll().get(0);
        List<TestCase> allTestCases = testCaseRepository.findByProblemId(problem.getId());
        List<TestCase> sampleTestCases = testCaseRepository.findByProblemIdAndIsSample(problem.getId(), true);
        List<TestCase> hiddenTestCases = testCaseRepository.findByProblemIdAndIsSample(problem.getId(), false);

        assertThat(problem.getSampleInput()).isNotBlank();
        assertThat(problem.getSampleOutput()).isNotBlank();
        assertThat(allTestCases).hasSize(EXPECTED_TEST_CASES_PER_PROBLEM);
        assertThat(sampleTestCases).hasSize(EXPECTED_SAMPLE_TEST_CASES_PER_PROBLEM);
        assertThat(hiddenTestCases).hasSize(EXPECTED_HIDDEN_TEST_CASES_PER_PROBLEM);
        assertThat(allTestCases)
                .allSatisfy(testCase -> {
                    assertThat(testCase.getInput()).isNotBlank();
                    assertThat(testCase.getExpectedOutput()).isNotBlank();
                });
    }

    @Test
    void dailyTemperatureTestCasesStayWithinDeclaredConstraints() {
        List<Problem> dailyTemperatureProblems = problemRepository.findAll().stream()
                .filter(problem -> problem.getTitle().startsWith("Daily Temperatures"))
                .toList();

        assertThat(dailyTemperatureProblems).hasSize(40);

        for (Problem problem : dailyTemperatureProblems) {
            assertThat(testCaseRepository.findByProblemId(problem.getId()))
                    .allSatisfy(testCase -> assertDailyTemperatureInput(testCase.getInput()));
        }
    }

    @Test
    void twoSumTestCasesHaveExactlyOneDeclaredAnswer() {
        List<Problem> twoSumProblems = problemRepository.findAll().stream()
                .filter(problem -> problem.getTitle().startsWith("Two Sum Drill"))
                .toList();

        assertThat(twoSumProblems).hasSize(40);

        for (Problem problem : twoSumProblems) {
            assertThat(testCaseRepository.findByProblemId(problem.getId()))
                    .allSatisfy(this::assertTwoSumHasExactlyOneDeclaredAnswer);
        }
    }

    private void assertTwoSumHasExactlyOneDeclaredAnswer(TestCase testCase) {
        String[] lines = testCase.getInput().split("\\R");
        int n = Integer.parseInt(lines[0].trim());
        int[] nums = parseInts(lines[1]);
        int target = Integer.parseInt(lines[2].trim());
        int[] expected = parseInts(testCase.getExpectedOutput());

        assertThat(nums).hasSize(n);
        assertThat(expected).hasSize(2);
        assertThat(nums[expected[0]] + nums[expected[1]]).isEqualTo(target);

        int pairs = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    pairs++;
                }
            }
        }
        assertThat(pairs).isEqualTo(1);
    }

    private void assertDailyTemperatureInput(String input) {
        String[] lines = input.split("\\R");
        int n = Integer.parseInt(lines[0].trim());
        int[] values = parseInts(lines[1]);

        assertThat(values).hasSize(n);
        for (int value : values) {
            assertThat(value).isBetween(30, 100);
        }
    }

    private int[] parseInts(String line) {
        return Arrays.stream(line.trim().split("\\s+"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
