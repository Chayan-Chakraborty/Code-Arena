package com.codearena.config;

import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.TestCaseRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class ProblemBankSeeder implements ApplicationRunner {

    private static final int VARIANTS_PER_FAMILY = 40;
    private static final int TARGET_PROBLEM_COUNT = 33 * VARIANTS_PER_FAMILY;

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemBankSeeder(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (problemRepository.count() >= TARGET_PROBLEM_COUNT) {
            return;
        }

        testCaseRepository.deleteAllInBatch();
        problemRepository.deleteAllInBatch();

        List<GeneratedProblem> problems = generateProblemBank();
        for (GeneratedProblem generated : problems) {
            Problem saved = problemRepository.save(Problem.builder()
                    .title(generated.title)
                    .description(generated.description)
                    .difficulty(generated.difficulty)
                    .topic(generated.topic)
                    .constraints(generated.constraints)
                    .sampleInput(generated.tests.get(0).input)
                    .sampleOutput(generated.tests.get(0).expectedOutput)
                    .starterCode(generated.starterCode)
                    .build());

            List<TestCase> testCases = generated.tests.stream()
                    .map(test -> TestCase.builder()
                            .problemId(saved.getId())
                            .input(test.input)
                            .expectedOutput(test.expectedOutput)
                            .isSample(test.sample)
                            .build())
                    .toList();
            testCaseRepository.saveAll(testCases);
        }
    }

    private List<GeneratedProblem> generateProblemBank() {
        List<GeneratedProblem> problems = new ArrayList<>(TARGET_PROBLEM_COUNT);
        for (int variant = 1; variant <= VARIANTS_PER_FAMILY; variant++) {
            problems.add(generateTwoSum(variant));
            problems.add(generateMaximumSubarray(variant));
            problems.add(generateMoveZeroes(variant));
            problems.add(generateContainsDuplicate(variant));
            problems.add(generateBestTimeToBuyStock(variant));
            problems.add(generateBinarySearch(variant));
            problems.add(generateRotateArray(variant));
            problems.add(generateProductExceptSelf(variant));
            problems.add(generateRangeSumQuery(variant));
            problems.add(generateMinSizeSubarraySum(variant));
            problems.add(generateReverseString(variant));
            problems.add(generateValidAnagram(variant));
            problems.add(generateLongestCommonPrefix(variant));
            problems.add(generateFirstUniqueCharacter(variant));
            problems.add(generateLongestSubstringWithoutRepeating(variant));
            problems.add(generateValidParentheses(variant));
            problems.add(generateNextGreaterElement(variant));
            problems.add(generateDailyTemperatures(variant));
            problems.add(generateFibonacci(variant));
            problems.add(generateClimbingStairs(variant));
            problems.add(generateMinCostClimbingStairs(variant));
            problems.add(generateHouseRobber(variant));
            problems.add(generateMatrixDiagonalSum(variant));
            problems.add(generateSearch2DMatrix(variant));
            problems.add(generateSpiralMatrix(variant));
            problems.add(generateReverseLinkedList(variant));
            problems.add(generateMiddleOfLinkedList(variant));
            problems.add(generateMergeTwoSortedLists(variant));
            problems.add(generateMaximumDepthBinaryTree(variant));
            problems.add(generateBinaryTreeInorderTraversal(variant));
            problems.add(generateSameTree(variant));
            problems.add(generateLastStoneWeight(variant));
            problems.add(generateNumberOfIslands(variant));
        }

        if (problems.size() != TARGET_PROBLEM_COUNT) {
            throw new IllegalStateException("Expected " + TARGET_PROBLEM_COUNT + " generated problems but found "
                    + problems.size());
        }

        return problems;
    }

    private GeneratedProblem generateTwoSum(int variant) {
        Random rnd = randomFor(1, variant);
        List<SeedTestCase> tests = List.of(
                new SeedTestCase(twoSumInput(new int[] { 2 + variant, 7, 11 + variant, 15 + variant }, 9 + variant),
                        "0 1", true),
                new SeedTestCase(twoSumInput(new int[] { 1, 2 + variant, 4 + variant }, 6 + (2 * variant)),
                        "1 2", true),
                new SeedTestCase(twoSumInput(new int[] { variant, variant }, variant * 2), "0 1", false),
                twoSumUniqueCase(rnd, 6 + (variant % 4), -12, 18, false),
                twoSumUniqueCase(rnd, 8 + (variant % 5), -25, 30, false));

        return problem("Two Sum Drill " + pad(variant),
                """
                        Given an integer array nums and an integer target, return the 0-based indices of the two
                        numbers such that they add up to target. Exactly one valid answer exists.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Line 3: target
                        Output format:
                        Print the two indices in increasing order.
                        """,
                "Easy", "Arrays",
                """
                        2 <= n <= 10^4
                        -10^9 <= nums[i] <= 10^9
                        Exactly one answer exists.
                        """,
                starterTwoSum(), tests);
    }

    private GeneratedProblem generateMaximumSubarray(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[] { -2, 1 + variant, -3, 4, -1, 2, 1, -5, 4 }, true, this::maxSubArray),
                arrayIntResultCase(new int[] { -3, -1, -2, -4 - variant }, true, this::maxSubArray),
                arrayIntResultCase(new int[] { variant, variant + 1, variant + 2 }, false, this::maxSubArray),
                arrayIntResultCase(new int[] { 5, -2, 3, 4, -1, 2 }, false, this::maxSubArray),
                arrayIntResultCase(new int[] { -10, 2, 3, -2, 5, -20, 8, 9 }, false, this::maxSubArray));

        return problem("Maximum Subarray Lab " + pad(variant),
                """
                        Find the contiguous subarray with the largest sum and return that sum.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Output format:
                        Print the maximum subarray sum.
                        """,
                "Medium", "Arrays",
                """
                        1 <= n <= 10^5
                        -10^4 <= nums[i] <= 10^4
                        """,
                starterMaximumSubarray(), tests);
    }

    private GeneratedProblem generateMoveZeroes(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayArrayResultCase(new int[] { 0, 1 + variant, 0, 3, 12 }, true, this::moveZeroes),
                arrayArrayResultCase(new int[] { 0 }, true, this::moveZeroes),
                arrayArrayResultCase(new int[] { 0, 0, 0, 1 + variant }, false, this::moveZeroes),
                arrayArrayResultCase(new int[] { 4, 2, 4, 0, 0, 3 }, false, this::moveZeroes),
                arrayArrayResultCase(new int[] { 1, 2, 3, 4 }, false, this::moveZeroes));

        return problem("Move Zeroes Workshop " + pad(variant),
                """
                        Move all zeroes in the array to the end while keeping the relative order of non-zero values.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Output format:
                        Print the transformed array.
                        """,
                "Easy", "Arrays",
                """
                        1 <= n <= 10^4
                        -2^31 <= nums[i] <= 2^31 - 1
                        """,
                starterMoveZeroes(), tests);
    }

    private GeneratedProblem generateContainsDuplicate(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayBooleanCase(new int[] { 1, 2, 3, 1 + (variant % 2) }, true, this::containsDuplicate),
                arrayBooleanCase(new int[] { 1, 2, 3, 4 + variant }, true, this::containsDuplicate),
                arrayBooleanCase(new int[] { variant, variant }, false, this::containsDuplicate),
                arrayBooleanCase(new int[] { -1, -2, -3, -4, -5 }, false, this::containsDuplicate),
                arrayBooleanCase(new int[] { 9, 8, 7, 6, 5, 4, 3, 2, 1, 9 }, false, this::containsDuplicate));

        return problem("Contains Duplicate Check " + pad(variant),
                """
                        Return true if any value appears at least twice in the array, otherwise return false.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Output format:
                        Print true or false.
                        """,
                "Easy", "Hashing",
                """
                        1 <= n <= 10^5
                        -10^9 <= nums[i] <= 10^9
                        """,
                starterContainsDuplicate(), tests);
    }

    private GeneratedProblem generateBestTimeToBuyStock(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[] { 7, 1, 5 + variant, 3, 6, 4 }, true, this::maxProfit),
                arrayIntResultCase(new int[] { 7, 6, 4, 3, 1 }, true, this::maxProfit),
                arrayIntResultCase(new int[] { 5 }, false, this::maxProfit),
                arrayIntResultCase(new int[] { 2, 4, 1, 7 }, false, this::maxProfit),
                arrayIntResultCase(new int[] { 3, 2, 6, 5, 0, 3 }, false, this::maxProfit));

        return problem("Best Stock Profit " + pad(variant),
                """
                        You may buy the stock once and sell it once later. Return the maximum profit, or 0 if no
                        profitable transaction exists.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated prices
                        Output format:
                        Print the maximum profit.
                        """,
                "Easy", "Arrays",
                """
                        1 <= n <= 10^5
                        0 <= prices[i] <= 10^4
                        """,
                starterBestStock(), tests);
    }

    private GeneratedProblem generateBinarySearch(int variant) {
        int[] sample1 = sortedDistinct(rangedArray(variant, 6, 2), 0);
        int target1 = sample1[sample1.length / 2];
        int[] sample2 = sortedDistinct(rangedArray(variant + 3, 5, 3), 0);
        int target2 = sample2[sample2.length - 1] + 1;
        List<SeedTestCase> tests = List.of(
                new SeedTestCase(binarySearchInput(sample1, target1), String.valueOf(binarySearch(sample1, target1)),
                        true),
                new SeedTestCase(binarySearchInput(sample2, target2), String.valueOf(binarySearch(sample2, target2)),
                        true),
                new SeedTestCase(binarySearchInput(new int[] { 5 }, 5), "0", false),
                new SeedTestCase(binarySearchInput(new int[] { -9, -4, -1, 3, 8 }, -9), "0", false),
                new SeedTestCase(binarySearchInput(new int[] { -9, -4, -1, 3, 8 }, 7), "-1", false));

        return problem("Binary Search Basics " + pad(variant),
                """
                        Given a sorted array and a target, return its index or -1 if it is missing.

                        Input format:
                        Line 1: n
                        Line 2: n sorted integers
                        Line 3: target
                        Output format:
                        Print the matching index or -1.
                        """,
                "Easy", "Binary Search",
                """
                        1 <= n <= 10^5
                        nums is sorted in strictly increasing order.
                        """,
                starterBinarySearch(), tests);
    }

    private GeneratedProblem generateRotateArray(int variant) {
        List<SeedTestCase> tests = List.of(
                rotateArrayCase(new int[] { 1, 2, 3, 4, 5, 6, 7 }, 3 + (variant % 2), true),
                rotateArrayCase(new int[] { -1, -100, 3, 99 }, 2, true),
                rotateArrayCase(new int[] { 1 }, 10, false),
                rotateArrayCase(new int[] { 1, 2, 3, 4 }, 4, false),
                rotateArrayCase(new int[] { 9, 8, 7, 6, 5 }, 12, false));

        return problem("Rotate Array Drill " + pad(variant),
                """
                        Rotate the array to the right by k steps.

                        Input format:
                        Line 1: n
                        Line 2: n integers
                        Line 3: k
                        Output format:
                        Print the rotated array.
                        """,
                "Medium", "Arrays",
                """
                        1 <= n <= 10^5
                        0 <= k <= 10^9
                        """,
                starterRotateArray(), tests);
    }

    private GeneratedProblem generateProductExceptSelf(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayArrayResultCase(new int[] { 1, 2, 3, 4 }, true, this::productExceptSelf),
                arrayArrayResultCase(new int[] { -1, 1, 0, -3, 3 }, true, this::productExceptSelf),
                arrayArrayResultCase(new int[] { variant, 1 }, false, this::productExceptSelf),
                arrayArrayResultCase(new int[] { 2, 3, 0, 4 }, false, this::productExceptSelf),
                arrayArrayResultCase(new int[] { 1, 1, 1, 1, 1 }, false, this::productExceptSelf));

        return problem("Product Except Self " + pad(variant),
                """
                        For each index, return the product of every other element in the array.

                        Input format:
                        Line 1: n
                        Line 2: n integers
                        Output format:
                        Print the result array.
                        """,
                "Medium", "Arrays",
                """
                        2 <= n <= 10^5
                        Values are chosen so intermediate products stay within 32-bit signed range.
                        """,
                starterProductExceptSelf(), tests);
    }

    private GeneratedProblem generateRangeSumQuery(int variant) {
        List<SeedTestCase> tests = List.of(
                rangeSumCase(new int[] { 1, 2, 3, 4, 5 }, 1, 3, true),
                rangeSumCase(new int[] { -2, 0, 3, -5, 2, -1 }, 0, 2, true),
                rangeSumCase(new int[] { 7 }, 0, 0, false),
                rangeSumCase(new int[] { 5, 4, 3, 2, 1 }, 2, 4, false),
                rangeSumCase(new int[] { 10, -10, 10, -10, 10 }, 0, 4, false));

        return problem("Range Sum Query Lite " + pad(variant),
                """
                        Return the sum of the array values from index left to index right inclusive.

                        Input format:
                        Line 1: n
                        Line 2: n integers
                        Line 3: left right
                        Output format:
                        Print the inclusive range sum.
                        """,
                "Easy", "Prefix Sum",
                """
                        1 <= n <= 10^5
                        0 <= left <= right < n
                        """,
                starterRangeSumQuery(), tests);
    }

    private GeneratedProblem generateMinSizeSubarraySum(int variant) {
        List<SeedTestCase> tests = List.of(
                minSizeSubarrayCase(7 + variant, new int[] { 2, 3, 1, 2, 4, 3, variant }, true),
                minSizeSubarrayCase(4, new int[] { 1, 4, 4 }, true),
                minSizeSubarrayCase(100, new int[] { 1, 2, 3, 4 }, false),
                minSizeSubarrayCase(11, new int[] { 1, 2, 3, 4, 5 }, false),
                minSizeSubarrayCase(15, new int[] { 5, 1, 3, 5, 10, 7, 4, 9, 2, 8 }, false));

        return problem("Minimum Size Subarray Sum " + pad(variant),
                """
                        Find the smallest length of a contiguous subarray whose sum is at least target. Return 0 if
                        no such subarray exists.

                        Input format:
                        Line 1: n
                        Line 2: n positive integers
                        Line 3: target
                        Output format:
                        Print the minimum valid length.
                        """,
                "Medium", "Sliding Window",
                """
                        1 <= n <= 10^5
                        1 <= nums[i] <= 10^4
                        1 <= target <= 10^9
                        """,
                starterMinSizeSubarraySum(), tests);
    }

    private GeneratedProblem generateReverseString(int variant) {
        List<SeedTestCase> tests = List.of(
                stringStringCase("hello" + suffixLetter(variant), true, this::reverseString),
                stringStringCase("a", true, this::reverseString),
                stringStringCase("racecar", false, this::reverseString),
                stringStringCase("codearena", false, this::reverseString),
                stringStringCase(randomLowercase(randomFor(11, variant), 8), false, this::reverseString));

        return problem("Reverse String Practice " + pad(variant),
                """
                        Return the reversed version of the given lowercase string.

                        Input format:
                        Line 1: s
                        Output format:
                        Print the reversed string.
                        """,
                "Easy", "Strings",
                """
                        1 <= |s| <= 10^5
                        s contains lowercase English letters only.
                        """,
                starterReverseString(), tests);
    }

    private GeneratedProblem generateValidAnagram(int variant) {
        Random rnd = randomFor(12, variant);
        String base = randomLowercase(rnd, 6);
        String sorted = sortLetters(base);
        String altered = makeNonAnagram(sorted);
        List<SeedTestCase> tests = List.of(
                twoStringBooleanCase("anagram", "nagaram", true, this::isAnagram),
                twoStringBooleanCase("rat", "car", true, this::isAnagram),
                twoStringBooleanCase(base, shuffleLetters(base, rnd), false, this::isAnagram),
                twoStringBooleanCase(sorted, altered, false, this::isAnagram),
                twoStringBooleanCase("aabbcc", "abcabc", false, this::isAnagram));

        return problem("Valid Anagram Review " + pad(variant),
                """
                        Return true if t is an anagram of s, otherwise return false.

                        Input format:
                        Line 1: s
                        Line 2: t
                        Output format:
                        Print true or false.
                        """,
                "Easy", "Strings",
                """
                        1 <= |s|, |t| <= 5 * 10^4
                        s and t contain lowercase English letters only.
                        """,
                starterValidAnagram(), tests);
    }

    private GeneratedProblem generateLongestCommonPrefix(int variant) {
        List<SeedTestCase> tests = List.of(
                stringArrayCase(new String[] { "flower", "flow", "flight" }, true, this::longestCommonPrefix),
                stringArrayCase(new String[] { "dog", "racecar", "car" }, true, this::longestCommonPrefix),
                stringArrayCase(new String[] { "interview", "internet", "internal", "into" }, false,
                        this::longestCommonPrefix),
                stringArrayCase(new String[] { "a" }, false, this::longestCommonPrefix),
                stringArrayCase(new String[] { "code" + suffixLetter(variant), "coding", "coder" }, false,
                        this::longestCommonPrefix));

        return problem("Longest Common Prefix " + pad(variant),
                """
                        Return the longest common prefix shared by all words. Print an empty line if no common prefix
                        exists.

                        Input format:
                        Line 1: m
                        Next m lines: one lowercase string per line
                        Output format:
                        Print the longest common prefix.
                        """,
                "Easy", "Strings",
                """
                        1 <= m <= 200
                        1 <= |word| <= 200
                        """,
                starterLongestCommonPrefix(), tests);
    }

    private GeneratedProblem generateFirstUniqueCharacter(int variant) {
        List<SeedTestCase> tests = List.of(
                stringIntCase("leetcode", true, this::firstUniqChar),
                stringIntCase("loveleetcode", true, this::firstUniqChar),
                stringIntCase("aabb", false, this::firstUniqChar),
                stringIntCase("z", false, this::firstUniqChar),
                stringIntCase("abacabad" + suffixLetter(variant), false, this::firstUniqChar));

        return problem("First Unique Character " + pad(variant),
                """
                        Return the index of the first non-repeating character in the string, or -1 if every character
                        repeats.

                        Input format:
                        Line 1: s
                        Output format:
                        Print the first unique index or -1.
                        """,
                "Easy", "Strings",
                """
                        1 <= |s| <= 10^5
                        s contains lowercase English letters only.
                        """,
                starterFirstUniqueCharacter(), tests);
    }

    private GeneratedProblem generateLongestSubstringWithoutRepeating(int variant) {
        List<SeedTestCase> tests = List.of(
                stringIntCase("abcabcbb", true, this::lengthOfLongestSubstring),
                stringIntCase("bbbbb", true, this::lengthOfLongestSubstring),
                stringIntCase("pwwkew", false, this::lengthOfLongestSubstring),
                stringIntCase("dvdf", false, this::lengthOfLongestSubstring),
                stringIntCase("abba" + suffixLetter(variant), false, this::lengthOfLongestSubstring));

        return problem("Longest Unique Substring " + pad(variant),
                """
                        Return the length of the longest substring that contains no repeated characters.

                        Input format:
                        Line 1: s
                        Output format:
                        Print the maximum length.
                        """,
                "Medium", "Sliding Window",
                """
                        1 <= |s| <= 10^5
                        s contains lowercase English letters only.
                        """,
                starterLongestSubstringWithoutRepeating(), tests);
    }

    private GeneratedProblem generateValidParentheses(int variant) {
        List<SeedTestCase> tests = List.of(
                stringBooleanCase("()[]{}", true, this::isValidParentheses),
                stringBooleanCase("(]", true, this::isValidParentheses),
                stringBooleanCase("({[]})", false, this::isValidParentheses),
                stringBooleanCase("(((", false, this::isValidParentheses),
                stringBooleanCase("([{}]){}[]", false, this::isValidParentheses));

        return problem("Valid Parentheses " + pad(variant),
                """
                        Return true if every bracket is closed in the correct order.

                        Input format:
                        Line 1: s
                        Output format:
                        Print true or false.
                        """,
                "Easy", "Stack",
                """
                        1 <= |s| <= 10^5
                        s consists only of ()[]{} characters.
                        """,
                starterValidParentheses(), tests);
    }

    private GeneratedProblem generateNextGreaterElement(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayArrayResultCase(new int[] { 2, 1, 2, 4, 3 }, true, this::nextGreaterElementsRight),
                arrayArrayResultCase(new int[] { 1, 3, 4, 2 }, true, this::nextGreaterElementsRight),
                arrayArrayResultCase(new int[] { 5 }, false, this::nextGreaterElementsRight),
                arrayArrayResultCase(new int[] { 9, 8, 7, 6 }, false, this::nextGreaterElementsRight),
                arrayArrayResultCase(new int[] { 1, 5, 3, 6, 8, 2, variant + 9 }, false,
                        this::nextGreaterElementsRight));

        return problem("Next Greater Element " + pad(variant),
                """
                        For each array value, find the first greater value to its right. If no such value exists,
                        print -1 for that index.

                        Input format:
                        Line 1: n
                        Line 2: n integers
                        Output format:
                        Print the answer array.
                        """,
                "Easy", "Stack",
                """
                        1 <= n <= 10^5
                        """,
                starterNextGreaterElement(), tests);
    }

    private GeneratedProblem generateDailyTemperatures(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayArrayResultCase(new int[] { 73, 74, 75, 71, 69, 72, 76, 73 }, true, this::dailyTemperatures),
                arrayArrayResultCase(new int[] { 30, 40, 50, 60 }, true, this::dailyTemperatures),
                arrayArrayResultCase(new int[] { 30, 60, 90 }, false, this::dailyTemperatures),
                arrayArrayResultCase(new int[] { 90, 80, 70, 60 }, false, this::dailyTemperatures),
                arrayArrayResultCase(new int[] { 65, 65, 66, 64, 70, 68, 72 + (variant % 20) }, false,
                        this::dailyTemperatures));

        return problem("Daily Temperatures " + pad(variant),
                """
                        For each day, return how many days you must wait until a warmer temperature appears. Return 0
                        if there is no future warmer day.

                        Input format:
                        Line 1: n
                        Line 2: n temperatures
                        Output format:
                        Print the waiting-days array.
                        """,
                "Medium", "Stack",
                """
                        1 <= n <= 10^5
                        30 <= temperatures[i] <= 100
                        """,
                starterDailyTemperatures(), tests);
    }

    private GeneratedProblem generateFibonacci(int variant) {
        List<SeedTestCase> tests = List.of(
                intIntCase(5 + (variant % 4), true, this::fib),
                intIntCase(0, true, this::fib),
                intIntCase(1, false, this::fib),
                intIntCase(10, false, this::fib),
                intIntCase(20, false, this::fib));

        return problem("Fibonacci Number " + pad(variant),
                """
                        Return F(n) where F(0)=0, F(1)=1, and F(n)=F(n-1)+F(n-2).

                        Input format:
                        Line 1: n
                        Output format:
                        Print F(n).
                        """,
                "Easy", "Dynamic Programming",
                """
                        0 <= n <= 30
                        """,
                starterFibonacci(), tests);
    }

    private GeneratedProblem generateClimbingStairs(int variant) {
        List<SeedTestCase> tests = List.of(
                intIntCase(2 + (variant % 3), true, this::climbStairs),
                intIntCase(5, true, this::climbStairs),
                intIntCase(1, false, this::climbStairs),
                intIntCase(10, false, this::climbStairs),
                intIntCase(15, false, this::climbStairs));

        return problem("Climbing Stairs " + pad(variant),
                """
                        You may climb either 1 or 2 steps at a time. Return the number of distinct ways to reach the
                        top.

                        Input format:
                        Line 1: n
                        Output format:
                        Print the number of ways.
                        """,
                "Easy", "Dynamic Programming",
                """
                        1 <= n <= 45
                        """,
                starterClimbingStairs(), tests);
    }

    private GeneratedProblem generateMinCostClimbingStairs(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[] { 10, 15, 20 }, true, this::minCostClimbingStairs),
                arrayIntResultCase(new int[] { 1, 100, 1, 1, 1, 100, 1, 1, 100, 1 }, true,
                        this::minCostClimbingStairs),
                arrayIntResultCase(new int[] { 0, 0, 0, 0 }, false, this::minCostClimbingStairs),
                arrayIntResultCase(new int[] { variant, 2, 2, 1 }, false, this::minCostClimbingStairs),
                arrayIntResultCase(new int[] { 5, 6 }, false, this::minCostClimbingStairs));

        return problem("Min Cost Climbing Stairs " + pad(variant),
                """
                        Each value is the cost to step on that stair. You may start from step 0 or step 1 and can
                        move up 1 or 2 steps at a time. Return the minimum total cost to reach the top.

                        Input format:
                        Line 1: n
                        Line 2: n costs
                        Output format:
                        Print the minimum cost.
                        """,
                "Easy", "Dynamic Programming",
                """
                        2 <= n <= 10^5
                        0 <= cost[i] <= 10^4
                        """,
                starterMinCostClimbingStairs(), tests);
    }

    private GeneratedProblem generateHouseRobber(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[] { 1, 2, 3, 1 }, true, this::houseRobber),
                arrayIntResultCase(new int[] { 2, 7, 9, 3, 1 }, true, this::houseRobber),
                arrayIntResultCase(new int[] { 5 }, false, this::houseRobber),
                arrayIntResultCase(new int[] { 2, 1, 1, 2 }, false, this::houseRobber),
                arrayIntResultCase(new int[] { variant, 3, 1, 3, 100 }, false, this::houseRobber));

        return problem("House Robber " + pad(variant),
                """
                        You cannot rob two adjacent houses. Return the maximum money you can rob.

                        Input format:
                        Line 1: n
                        Line 2: n non-negative integers
                        Output format:
                        Print the maximum amount.
                        """,
                "Medium", "Dynamic Programming",
                """
                        1 <= n <= 10^5
                        0 <= nums[i] <= 10^4
                        """,
                starterHouseRobber(), tests);
    }

    private GeneratedProblem generateMatrixDiagonalSum(int variant) {
        List<SeedTestCase> tests = List.of(
                matrixIntCase(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }, true, this::diagonalSum),
                matrixIntCase(new int[][] { { 5 } }, true, this::diagonalSum),
                matrixIntCase(new int[][] { { 1, 1 }, { 1, 1 } }, false, this::diagonalSum),
                matrixIntCase(new int[][] { { 7, 3, 1, 9 }, { 3, 4, 6, 9 }, { 6, 9, 6, 6 }, { 9, 5, 8, 5 } }, false,
                        this::diagonalSum),
                matrixIntCase(new int[][] { { variant, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }, false, this::diagonalSum));

        return problem("Matrix Diagonal Sum " + pad(variant),
                """
                        Return the sum of the primary and secondary diagonals of the square matrix. Do not double-count
                        the center cell.

                        Input format:
                        Line 1: n
                        Next n lines: n integers per line
                        Output format:
                        Print the diagonal sum.
                        """,
                "Easy", "Matrix",
                """
                        1 <= n <= 200
                        """,
                starterMatrixDiagonalSum(), tests);
    }

    private GeneratedProblem generateSearch2DMatrix(int variant) {
        List<SeedTestCase> tests = List.of(
                matrixTargetBooleanCase(new int[][] { { 1, 3, 5, 7 }, { 10, 11, 16, 20 }, { 23, 30, 34, 60 } }, 3, true,
                        this::searchMatrix),
                matrixTargetBooleanCase(new int[][] { { 1, 3, 5, 7 }, { 10, 11, 16, 20 }, { 23, 30, 34, 60 } }, 13,
                        true,
                        this::searchMatrix),
                matrixTargetBooleanCase(new int[][] { { 5 } }, 5, false, this::searchMatrix),
                matrixTargetBooleanCase(new int[][] { { 5 } }, 2, false, this::searchMatrix),
                matrixTargetBooleanCase(new int[][] { { 1, 2, 4 }, { 7, 9, 11 }, { 15, 18, 21 + variant } },
                        21 + variant,
                        false, this::searchMatrix));

        return problem("Search 2D Matrix " + pad(variant),
                """
                        The rows are sorted left-to-right and the first value of each row is greater than the last
                        value of the previous row. Return true if the target exists.

                        Input format:
                        Line 1: rows cols
                        Next rows lines: cols integers per line
                        Last line: target
                        Output format:
                        Print true or false.
                        """,
                "Medium", "Matrix",
                """
                        1 <= rows, cols <= 200
                        Matrix values are sorted in row-major order.
                        """,
                starterSearch2DMatrix(), tests);
    }

    private GeneratedProblem generateSpiralMatrix(int variant) {
        List<SeedTestCase> tests = List.of(
                matrixArrayCase(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }, true, this::spiralOrder),
                matrixArrayCase(new int[][] { { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 10, 11, 12 } }, true,
                        this::spiralOrder),
                matrixArrayCase(new int[][] { { 1 } }, false, this::spiralOrder),
                matrixArrayCase(new int[][] { { 1, 2, 3, 4 } }, false, this::spiralOrder),
                matrixArrayCase(new int[][] { { variant, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 } }, false,
                        this::spiralOrder));

        return problem("Spiral Matrix Tour " + pad(variant),
                """
                        Return the matrix values in clockwise spiral order.

                        Input format:
                        Line 1: rows cols
                        Next rows lines: cols integers per line
                        Output format:
                        Print the spiral traversal.
                        """,
                "Medium", "Matrix",
                """
                        1 <= rows, cols <= 100
                        """,
                starterSpiralMatrix(), tests);
    }

    private GeneratedProblem generateReverseLinkedList(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayArrayResultCase(new int[]{1, 2, 3, 4, 5}, true, this::reverseArray),
                arrayArrayResultCase(new int[]{1}, true, this::reverseArray),
                arrayArrayResultCase(new int[]{variant, variant + 1}, false, this::reverseArray),
                arrayArrayResultCase(new int[]{9, 8, 7, 6}, false, this::reverseArray),
                arrayArrayResultCase(new int[]{5, 5, 4, 4, 3}, false, this::reverseArray));

        return problem("Reverse Linked List " + pad(variant),
                """
                        Reverse a singly linked list and print the resulting node values from head to tail.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Output format:
                        Print the reversed list values.
                        """,
                "Easy", "Linked List",
                """
                        1 <= n <= 10^5
                        -10^9 <= node.val <= 10^9
                        """,
                starterReverseLinkedList(), tests);
    }

    private GeneratedProblem generateMiddleOfLinkedList(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[]{1, 2, 3, 4, 5}, true, this::middleNodeValue),
                arrayIntResultCase(new int[]{1, 2, 3, 4, 5, 6}, true, this::middleNodeValue),
                arrayIntResultCase(new int[]{variant}, false, this::middleNodeValue),
                arrayIntResultCase(new int[]{9, 8}, false, this::middleNodeValue),
                arrayIntResultCase(new int[]{4, 7, 1, 9, 2, 6, 8, 3}, false, this::middleNodeValue));

        return problem("Middle of Linked List " + pad(variant),
                """
                        Return the value of the middle node of the linked list. If there are two middle nodes, use the
                        second middle.

                        Input format:
                        Line 1: n
                        Line 2: n space-separated integers
                        Output format:
                        Print the middle node value.
                        """,
                "Easy", "Linked List",
                """
                        1 <= n <= 10^5
                        -10^9 <= node.val <= 10^9
                        """,
                starterMiddleOfLinkedList(), tests);
    }

    private GeneratedProblem generateMergeTwoSortedLists(int variant) {
        List<SeedTestCase> tests = List.of(
                mergeListsCase(new int[]{1, 2, 4}, new int[]{1, 3, 4}, true),
                mergeListsCase(new int[]{1, 2, 2 + variant}, new int[]{1, 1 + variant, 3 + variant}, true),
                mergeListsCase(new int[]{1}, new int[]{}, false),
                mergeListsCase(new int[]{}, new int[]{2, 5, 9}, false),
                mergeListsCase(new int[]{-3, 0, 7}, new int[]{-2, 4, 8, 10}, false));

        return problem("Merge Two Sorted Lists " + pad(variant),
                """
                        Merge two sorted linked lists into one sorted linked list and print the merged values.

                        Input format:
                        Line 1: n
                        Line 2: n sorted integers if n > 0, otherwise an empty line
                        Line 3: m
                        Line 4: m sorted integers if m > 0, otherwise an empty line
                        Output format:
                        Print the merged sorted list values.
                        """,
                "Easy", "Linked List",
                """
                        0 <= n, m <= 10^5
                        The individual lists are sorted in non-decreasing order.
                        """,
                starterMergeTwoSortedLists(), tests);
    }

    private GeneratedProblem generateMaximumDepthBinaryTree(int variant) {
        List<SeedTestCase> tests = List.of(
                treeIntCase(new String[]{"3", "9", "20", "null", "null", "15", "7"}, true, this::maxDepthTokens),
                treeIntCase(new String[]{"1", "null", "2"}, true, this::maxDepthTokens),
                treeIntCase(new String[]{String.valueOf(variant)}, false, this::maxDepthTokens),
                treeIntCase(new String[]{"1", "2", "3", "4", "5", "null", "null"}, false, this::maxDepthTokens),
                treeIntCase(new String[]{"1", "2", "null", "3", "null", "null", "null", "4"}, false,
                        this::maxDepthTokens));

        return problem("Maximum Depth of Binary Tree " + pad(variant),
                """
                        Return the maximum depth of the binary tree.

                        Input format:
                        Line 1: t
                        Line 2: t space-separated level-order tokens using null for missing nodes
                        Output format:
                        Print the maximum depth.
                        """,
                "Easy", "Trees",
                """
                        1 <= t <= 255
                        Tokens use level-order traversal with null placeholders.
                        """,
                starterMaximumDepthBinaryTree(), tests);
    }

    private GeneratedProblem generateBinaryTreeInorderTraversal(int variant) {
        List<SeedTestCase> tests = List.of(
                treeArrayCase(new String[]{"1", "null", "2", "null", "null", "3"}, true, this::inorderTokens),
                treeArrayCase(new String[]{"4", "2", "6", "1", "3", "5", "7"}, true, this::inorderTokens),
                treeArrayCase(new String[]{String.valueOf(variant)}, false, this::inorderTokens),
                treeArrayCase(new String[]{"5", "3", "8", "1", "4", "7", "9"}, false, this::inorderTokens),
                treeArrayCase(new String[]{"2", "1", "3", "null", "null", "null", "4"}, false, this::inorderTokens));

        return problem("Binary Tree Inorder Traversal " + pad(variant),
                """
                        Return the inorder traversal of the binary tree.

                        Input format:
                        Line 1: t
                        Line 2: t space-separated level-order tokens using null for missing nodes
                        Output format:
                        Print the inorder traversal values.
                        """,
                "Easy", "Trees",
                """
                        1 <= t <= 255
                        Tokens use level-order traversal with null placeholders.
                        """,
                starterBinaryTreeInorderTraversal(), tests);
    }

    private GeneratedProblem generateSameTree(int variant) {
        List<SeedTestCase> tests = List.of(
                sameTreeCase(new String[]{"1", "2", "3"}, new String[]{"1", "2", "3"}, true),
                sameTreeCase(new String[]{"1", "2"}, new String[]{"1", "null", "2"}, true),
                sameTreeCase(new String[]{String.valueOf(variant)}, new String[]{String.valueOf(variant)}, false),
                sameTreeCase(new String[]{"5", "3", "7"}, new String[]{"5", "3", "8"}, false),
                sameTreeCase(new String[]{"1", "2", "3", "4"}, new String[]{"1", "2", "3", "null", "4"}, false));

        return problem("Same Tree " + pad(variant),
                """
                        Return true if the two binary trees are structurally identical and contain the same values.

                        Input format:
                        Line 1: t1
                        Line 2: t1 space-separated level-order tokens for the first tree
                        Line 3: t2
                        Line 4: t2 space-separated level-order tokens for the second tree
                        Output format:
                        Print true or false.
                        """,
                "Easy", "Trees",
                """
                        1 <= t1, t2 <= 255
                        Tokens use level-order traversal with null placeholders.
                        """,
                starterSameTree(), tests);
    }

    private GeneratedProblem generateLastStoneWeight(int variant) {
        List<SeedTestCase> tests = List.of(
                arrayIntResultCase(new int[]{2, 7, 4, 1, 8, 1}, true, this::lastStoneWeight),
                arrayIntResultCase(new int[]{1}, true, this::lastStoneWeight),
                arrayIntResultCase(new int[]{variant, variant}, false, this::lastStoneWeight),
                arrayIntResultCase(new int[]{10, 4, 2, 10}, false, this::lastStoneWeight),
                arrayIntResultCase(new int[]{9, 3, 2, 10, 7, 1}, false, this::lastStoneWeight));

        return problem("Last Stone Weight " + pad(variant),
                """
                        Repeatedly smash the two heaviest stones together. Return the weight of the final remaining
                        stone, or 0 if none remain.

                        Input format:
                        Line 1: n
                        Line 2: n stone weights
                        Output format:
                        Print the last stone weight.
                        """,
                "Easy", "Heap",
                """
                        1 <= n <= 10^5
                        1 <= stones[i] <= 10^4
                        """,
                starterLastStoneWeight(), tests);
    }

    private GeneratedProblem generateNumberOfIslands(int variant) {
        List<SeedTestCase> tests = List.of(
                gridIntCase(new String[]{"11110", "11010", "11000", "00000"}, true, this::numberOfIslands),
                gridIntCase(new String[]{"11000", "11000", "00100", "00011"}, true, this::numberOfIslands),
                gridIntCase(new String[]{"1"}, false, this::numberOfIslands),
                gridIntCase(new String[]{"101", "010", "101"}, false, this::numberOfIslands),
                gridIntCase(new String[]{"111", "010", "1" + (variant % 2) + "1"}, false, this::numberOfIslands));

        return problem("Number of Islands " + pad(variant),
                """
                        Count the number of islands in the grid. Cells with value 1 are land, 0 are water, and
                        islands connect horizontally or vertically.

                        Input format:
                        Line 1: rows cols
                        Next rows lines: each line is a string of 0 and 1 characters
                        Output format:
                        Print the number of islands.
                        """,
                "Medium", "Graphs",
                """
                        1 <= rows, cols <= 200
                        Grid rows contain only 0 and 1 characters.
                        """,
                starterNumberOfIslands(), tests);
    }

    private GeneratedProblem problem(String title, String description, String difficulty, String topic,
            String constraints, String starterCode, List<SeedTestCase> tests) {
        return new GeneratedProblem(title, description.strip(), difficulty, topic, constraints.strip(),
                starterCode.strip(), tests);
    }

    private SeedTestCase twoSumUniqueCase(Random rnd, int n, int min, int max, boolean sample) {
        while (true) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = randomBetween(rnd, min, max);
            }
            int first = rnd.nextInt(n);
            int second = (first + 1 + rnd.nextInt(n - 1)) % n;
            int target = nums[first] + nums[second];
            int pairCount = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (nums[i] + nums[j] == target) {
                        pairCount++;
                    }
                }
            }
            if (pairCount == 1) {
                int[] answer = new int[] { Math.min(first, second), Math.max(first, second) };
                return new SeedTestCase(twoSumInput(nums, target), joinInts(answer), sample);
            }
        }
    }

    private SeedTestCase arrayIntResultCase(int[] nums, boolean sample, IntArraySolver solver) {
        return new SeedTestCase(arrayInput(nums), String.valueOf(solver.solve(nums.clone())), sample);
    }

    private SeedTestCase arrayBooleanCase(int[] nums, boolean sample, IntArrayBooleanSolver solver) {
        return new SeedTestCase(arrayInput(nums), String.valueOf(solver.solve(nums.clone())), sample);
    }

    private SeedTestCase arrayArrayResultCase(int[] nums, boolean sample, IntArrayTransform solver) {
        return new SeedTestCase(arrayInput(nums), joinInts(solver.solve(nums.clone())), sample);
    }

    private SeedTestCase rotateArrayCase(int[] nums, int k, boolean sample) {
        return new SeedTestCase(rotateInput(nums, k), joinInts(rotateArray(nums.clone(), k)), sample);
    }

    private SeedTestCase rangeSumCase(int[] nums, int left, int right, boolean sample) {
        return new SeedTestCase(rangeSumInput(nums, left, right), String.valueOf(rangeSum(nums, left, right)), sample);
    }

    private SeedTestCase minSizeSubarrayCase(int target, int[] nums, boolean sample) {
        return new SeedTestCase(minSizeInput(nums, target), String.valueOf(minSubArrayLen(target, nums)), sample);
    }

    private SeedTestCase stringStringCase(String input, boolean sample, StringSolver solver) {
        return new SeedTestCase(input, solver.solve(input), sample);
    }

    private SeedTestCase twoStringBooleanCase(String a, String b, boolean sample, TwoStringBooleanSolver solver) {
        return new SeedTestCase(a + "\n" + b, String.valueOf(solver.solve(a, b)), sample);
    }

    private SeedTestCase stringArrayCase(String[] words, boolean sample, StringArraySolver solver) {
        return new SeedTestCase(stringArrayInput(words), solver.solve(words.clone()), sample);
    }

    private SeedTestCase stringIntCase(String input, boolean sample, StringIntSolver solver) {
        return new SeedTestCase(input, String.valueOf(solver.solve(input)), sample);
    }

    private SeedTestCase stringBooleanCase(String input, boolean sample, StringBooleanSolver solver) {
        return new SeedTestCase(input, String.valueOf(solver.solve(input)), sample);
    }

    private SeedTestCase intIntCase(int value, boolean sample, IntSolver solver) {
        return new SeedTestCase(String.valueOf(value), String.valueOf(solver.solve(value)), sample);
    }

    private SeedTestCase matrixIntCase(int[][] matrix, boolean sample, MatrixIntSolver solver) {
        return new SeedTestCase(matrixInput(matrix), String.valueOf(solver.solve(copyMatrix(matrix))), sample);
    }

    private SeedTestCase matrixTargetBooleanCase(int[][] matrix, int target, boolean sample,
            MatrixTargetBooleanSolver solver) {
        return new SeedTestCase(matrixTargetInput(matrix, target),
                String.valueOf(solver.solve(copyMatrix(matrix), target)), sample);
    }

    private SeedTestCase matrixArrayCase(int[][] matrix, boolean sample, MatrixArraySolver solver) {
        return new SeedTestCase(matrixRectInput(matrix), joinInts(solver.solve(copyMatrix(matrix))), sample);
    }

    private SeedTestCase mergeListsCase(int[] first, int[] second, boolean sample) {
        return new SeedTestCase(mergeListsInput(first, second), joinInts(mergeSortedArrays(first, second)), sample);
    }

    private SeedTestCase treeIntCase(String[] tokens, boolean sample, TokenTreeIntSolver solver) {
        return new SeedTestCase(treeInput(tokens), String.valueOf(solver.solve(tokens.clone())), sample);
    }

    private SeedTestCase treeArrayCase(String[] tokens, boolean sample, TokenTreeArraySolver solver) {
        return new SeedTestCase(treeInput(tokens), joinInts(solver.solve(tokens.clone())), sample);
    }

    private SeedTestCase sameTreeCase(String[] first, String[] second, boolean sample) {
        return new SeedTestCase(twoTreeInput(first, second), String.valueOf(sameTree(first, second)), sample);
    }

    private SeedTestCase gridIntCase(String[] grid, boolean sample, GridIntSolver solver) {
        return new SeedTestCase(gridInput(grid), String.valueOf(solver.solve(grid.clone())), sample);
    }

    private String twoSumInput(int[] nums, int target) {
        return nums.length + "\n" + joinInts(nums) + "\n" + target;
    }

    private String binarySearchInput(int[] nums, int target) {
        return nums.length + "\n" + joinInts(nums) + "\n" + target;
    }

    private String arrayInput(int[] nums) {
        return nums.length + "\n" + joinInts(nums);
    }

    private String rotateInput(int[] nums, int k) {
        return nums.length + "\n" + joinInts(nums) + "\n" + k;
    }

    private String mergeListsInput(int[] first, int[] second) {
        return first.length + "\n" + maybeJoinInts(first) + "\n" + second.length + "\n" + maybeJoinInts(second);
    }

    private String rangeSumInput(int[] nums, int left, int right) {
        return nums.length + "\n" + joinInts(nums) + "\n" + left + " " + right;
    }

    private String minSizeInput(int[] nums, int target) {
        return nums.length + "\n" + joinInts(nums) + "\n" + target;
    }

    private String stringArrayInput(String[] words) {
        return words.length + "\n" + String.join("\n", words);
    }

    private String treeInput(String[] tokens) {
        return tokens.length + "\n" + String.join(" ", tokens);
    }

    private String twoTreeInput(String[] first, String[] second) {
        return first.length + "\n" + String.join(" ", first) + "\n" + second.length + "\n" + String.join(" ", second);
    }

    private String gridInput(String[] grid) {
        return grid.length + " " + grid[0].length() + "\n" + String.join("\n", grid);
    }

    private String matrixInput(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append(matrix.length).append("\n");
        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(joinInts(matrix[i]));
        }
        return sb.toString();
    }

    private String matrixRectInput(int[][] matrix) {
        return matrix.length + " " + matrix[0].length + "\n" + matrixRows(matrix);
    }

    private String matrixTargetInput(int[][] matrix, int target) {
        return matrix.length + " " + matrix[0].length + "\n" + matrixRows(matrix) + "\n" + target;
    }

    private String matrixRows(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(joinInts(matrix[i]));
        }
        return sb.toString();
    }

    private String joinInts(int[] nums) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nums.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(nums[i]);
        }
        return sb.toString();
    }

    private String maybeJoinInts(int[] nums) {
        return nums.length == 0 ? "" : joinInts(nums);
    }

    private String pad(int variant) {
        return String.format("%02d", variant);
    }

    private Random randomFor(int family, int variant) {
        return new Random(family * 10_000L + variant * 97L);
    }

    private int randomBetween(Random rnd, int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }

    private int[] rangedArray(int start, int length, int step) {
        int[] nums = new int[length];
        for (int i = 0; i < length; i++) {
            nums[i] = start + (i * step);
        }
        return nums;
    }

    private int[] sortedDistinct(int[] nums, int offset) {
        int[] copy = nums.clone();
        Arrays.sort(copy);
        for (int i = 0; i < copy.length; i++) {
            copy[i] += offset + i;
        }
        return copy;
    }

    private String randomLowercase(Random rnd, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rnd.nextInt(26)));
        }
        return sb.toString();
    }

    private String shuffleLetters(String s, Random rnd) {
        char[] chars = s.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    private String sortLetters(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private String makeNonAnagram(String s) {
        char[] chars = s.toCharArray();
        chars[0] = chars[0] == 'z' ? 'y' : 'z';
        return new String(chars);
    }

    private String suffixLetter(int variant) {
        return String.valueOf((char) ('a' + (variant % 26)));
    }

    private int[] moveZeroes(int[] nums) {
        int[] out = nums.clone();
        int write = 0;
        for (int num : out) {
            if (num != 0) {
                out[write++] = num;
            }
        }
        while (write < out.length) {
            out[write++] = 0;
        }
        return out;
    }

    private boolean containsDuplicate(int[] nums) {
        Arrays.sort(nums);
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] == nums[i - 1]) {
                return true;
            }
        }
        return false;
    }

    private int maxSubArray(int[] nums) {
        int best = nums[0];
        int current = nums[0];
        for (int i = 1; i < nums.length; i++) {
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    private int maxProfit(int[] prices) {
        int min = Integer.MAX_VALUE;
        int best = 0;
        for (int price : prices) {
            min = Math.min(min, price);
            best = Math.max(best, price - min);
        }
        return best;
    }

    private int binarySearch(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = left + ((right - left) / 2);
            if (nums[mid] == target) {
                return mid;
            }
            if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    private int[] rotateArray(int[] nums, int k) {
        int n = nums.length;
        int[] out = new int[n];
        int shift = k % n;
        for (int i = 0; i < n; i++) {
            out[(i + shift) % n] = nums[i];
        }
        return out;
    }

    private int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] out = new int[n];
        int prefix = 1;
        for (int i = 0; i < n; i++) {
            out[i] = prefix;
            prefix *= nums[i];
        }
        int suffix = 1;
        for (int i = n - 1; i >= 0; i--) {
            out[i] *= suffix;
            suffix *= nums[i];
        }
        return out;
    }

    private int rangeSum(int[] nums, int left, int right) {
        int sum = 0;
        for (int i = left; i <= right; i++) {
            sum += nums[i];
        }
        return sum;
    }

    private int minSubArrayLen(int target, int[] nums) {
        int left = 0;
        int sum = 0;
        int best = Integer.MAX_VALUE;
        for (int right = 0; right < nums.length; right++) {
            sum += nums[right];
            while (sum >= target) {
                best = Math.min(best, right - left + 1);
                sum -= nums[left++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    private String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    private boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) {
            freq[s.charAt(i) - 'a']++;
            freq[t.charAt(i) - 'a']--;
        }
        for (int count : freq) {
            if (count != 0) {
                return false;
            }
        }
        return true;
    }

    private String longestCommonPrefix(String[] words) {
        String prefix = words[0];
        for (int i = 1; i < words.length; i++) {
            while (!words[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return "";
                }
            }
        }
        return prefix;
    }

    private int firstUniqChar(String s) {
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) {
            freq[s.charAt(i) - 'a']++;
        }
        for (int i = 0; i < s.length(); i++) {
            if (freq[s.charAt(i) - 'a'] == 1) {
                return i;
            }
        }
        return -1;
    }

    private int lengthOfLongestSubstring(String s) {
        int[] last = new int[26];
        Arrays.fill(last, -1);
        int left = 0;
        int best = 0;
        for (int right = 0; right < s.length(); right++) {
            int idx = s.charAt(right) - 'a';
            if (last[idx] >= left) {
                left = last[idx] + 1;
            }
            last[idx] = right;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    private boolean isValidParentheses(String s) {
        char[] stack = new char[s.length()];
        int size = 0;
        for (char ch : s.toCharArray()) {
            if (ch == '(' || ch == '[' || ch == '{') {
                stack[size++] = ch;
            } else {
                if (size == 0) {
                    return false;
                }
                char top = stack[--size];
                if ((ch == ')' && top != '(') || (ch == ']' && top != '[') || (ch == '}' && top != '{')) {
                    return false;
                }
            }
        }
        return size == 0;
    }

    private int[] nextGreaterElementsRight(int[] nums) {
        int[] out = new int[nums.length];
        int[] stack = new int[nums.length];
        int size = 0;
        for (int i = nums.length - 1; i >= 0; i--) {
            while (size > 0 && stack[size - 1] <= nums[i]) {
                size--;
            }
            out[i] = size == 0 ? -1 : stack[size - 1];
            stack[size++] = nums[i];
        }
        return out;
    }

    private int[] dailyTemperatures(int[] temperatures) {
        int[] out = new int[temperatures.length];
        int[] stack = new int[temperatures.length];
        int size = 0;
        for (int i = temperatures.length - 1; i >= 0; i--) {
            while (size > 0 && temperatures[stack[size - 1]] <= temperatures[i]) {
                size--;
            }
            out[i] = size == 0 ? 0 : stack[size - 1] - i;
            stack[size++] = i;
        }
        return out;
    }

    private int fib(int n) {
        if (n <= 1) {
            return n;
        }
        int a = 0;
        int b = 1;
        for (int i = 2; i <= n; i++) {
            int next = a + b;
            a = b;
            b = next;
        }
        return b;
    }

    private int climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        int a = 1;
        int b = 2;
        for (int i = 3; i <= n; i++) {
            int next = a + b;
            a = b;
            b = next;
        }
        return b;
    }

    private int minCostClimbingStairs(int[] cost) {
        int prev2 = 0;
        int prev1 = 0;
        for (int c : cost) {
            int current = c + Math.min(prev1, prev2);
            prev2 = prev1;
            prev1 = current;
        }
        return Math.min(prev1, prev2);
    }

    private int houseRobber(int[] nums) {
        int include = 0;
        int exclude = 0;
        for (int num : nums) {
            int nextInclude = exclude + num;
            exclude = Math.max(exclude, include);
            include = nextInclude;
        }
        return Math.max(include, exclude);
    }

    private int diagonalSum(int[][] matrix) {
        int n = matrix.length;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += matrix[i][i];
            if (i != n - 1 - i) {
                sum += matrix[i][n - 1 - i];
            }
        }
        return sum;
    }

    private boolean searchMatrix(int[][] matrix, int target) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int left = 0;
        int right = rows * cols - 1;
        while (left <= right) {
            int mid = left + ((right - left) / 2);
            int value = matrix[mid / cols][mid % cols];
            if (value == target) {
                return true;
            }
            if (value < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return false;
    }

    private int[] spiralOrder(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[] out = new int[rows * cols];
        int index = 0;
        int top = 0;
        int bottom = rows - 1;
        int left = 0;
        int right = cols - 1;
        while (top <= bottom && left <= right) {
            for (int c = left; c <= right; c++) {
                out[index++] = matrix[top][c];
            }
            top++;
            for (int r = top; r <= bottom; r++) {
                out[index++] = matrix[r][right];
            }
            right--;
            if (top <= bottom) {
                for (int c = right; c >= left; c--) {
                    out[index++] = matrix[bottom][c];
                }
                bottom--;
            }
            if (left <= right) {
                for (int r = bottom; r >= top; r--) {
                    out[index++] = matrix[r][left];
                }
                left++;
            }
        }
        return out;
    }

    private int[] reverseArray(int[] nums) {
        int[] out = nums.clone();
        for (int left = 0, right = out.length - 1; left < right; left++, right--) {
            int tmp = out[left];
            out[left] = out[right];
            out[right] = tmp;
        }
        return out;
    }

    private int middleNodeValue(int[] nums) {
        return nums[nums.length / 2];
    }

    private int[] mergeSortedArrays(int[] first, int[] second) {
        int[] out = new int[first.length + second.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < first.length && j < second.length) {
            if (first[i] <= second[j]) {
                out[k++] = first[i++];
            } else {
                out[k++] = second[j++];
            }
        }
        while (i < first.length) {
            out[k++] = first[i++];
        }
        while (j < second.length) {
            out[k++] = second[j++];
        }
        return out;
    }

    private int maxDepthTokens(String[] tokens) {
        return maxDepthAt(tokens, 0);
    }

    private int maxDepthAt(String[] tokens, int index) {
        if (index >= tokens.length || "null".equals(tokens[index])) {
            return 0;
        }
        return 1 + Math.max(maxDepthAt(tokens, index * 2 + 1), maxDepthAt(tokens, index * 2 + 2));
    }

    private int[] inorderTokens(String[] tokens) {
        List<Integer> out = new ArrayList<>();
        inorderAt(tokens, 0, out);
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    private void inorderAt(String[] tokens, int index, List<Integer> out) {
        if (index >= tokens.length || "null".equals(tokens[index])) {
            return;
        }
        inorderAt(tokens, index * 2 + 1, out);
        out.add(Integer.parseInt(tokens[index]));
        inorderAt(tokens, index * 2 + 2, out);
    }

    private boolean sameTree(String[] first, String[] second) {
        return sameTreeAt(first, second, 0, 0);
    }

    private boolean sameTreeAt(String[] first, String[] second, int i, int j) {
        boolean firstNull = i >= first.length || "null".equals(first[i]);
        boolean secondNull = j >= second.length || "null".equals(second[j]);
        if (firstNull || secondNull) {
            return firstNull == secondNull;
        }
        if (!first[i].equals(second[j])) {
            return false;
        }
        return sameTreeAt(first, second, i * 2 + 1, j * 2 + 1)
                && sameTreeAt(first, second, i * 2 + 2, j * 2 + 2);
    }

    private int lastStoneWeight(int[] stones) {
        java.util.PriorityQueue<Integer> pq = new java.util.PriorityQueue<>(java.util.Collections.reverseOrder());
        for (int stone : stones) {
            pq.offer(stone);
        }
        while (pq.size() > 1) {
            int a = pq.poll();
            int b = pq.poll();
            if (a != b) {
                pq.offer(a - b);
            }
        }
        return pq.isEmpty() ? 0 : pq.peek();
    }

    private int numberOfIslands(String[] grid) {
        int rows = grid.length;
        int cols = grid[0].length();
        boolean[][] seen = new boolean[rows][cols];
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!seen[r][c] && grid[r].charAt(c) == '1') {
                    count++;
                    floodFill(grid, seen, r, c);
                }
            }
        }
        return count;
    }

    private void floodFill(String[] grid, boolean[][] seen, int row, int col) {
        if (row < 0 || col < 0 || row >= grid.length || col >= grid[0].length()) {
            return;
        }
        if (seen[row][col] || grid[row].charAt(col) != '1') {
            return;
        }
        seen[row][col] = true;
        floodFill(grid, seen, row + 1, col);
        floodFill(grid, seen, row - 1, col);
        floodFill(grid, seen, row, col + 1);
        floodFill(grid, seen, row, col - 1);
    }

    private int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    private String starterTwoSum() {
        return """
                import java.util.*;

                class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        // TODO: return the two matching indices
                        return new int[]{-1, -1};
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int target = sc.nextInt();
                        int[] ans = new Solution().twoSum(nums, target);
                        Arrays.sort(ans);
                        System.out.println(ans[0] + " " + ans[1]);
                    }
                }
                """;
    }

    private String starterMaximumSubarray() {
        return """
                import java.util.*;

                class Solution {
                    public int maxSubArray(int[] nums) {
                        // TODO: return the best contiguous sum
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        System.out.println(new Solution().maxSubArray(nums));
                    }
                }
                """;
    }

    private String starterMoveZeroes() {
        return """
                import java.util.*;

                class Solution {
                    public void moveZeroes(int[] nums) {
                        // TODO: modify nums in place
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        new Solution().moveZeroes(nums);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < n; i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(nums[i]);
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterContainsDuplicate() {
        return """
                import java.util.*;

                class Solution {
                    public boolean containsDuplicate(int[] nums) {
                        // TODO: return true if any value repeats
                        return false;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        System.out.println(new Solution().containsDuplicate(nums));
                    }
                }
                """;
    }

    private String starterBestStock() {
        return """
                import java.util.*;

                class Solution {
                    public int maxProfit(int[] prices) {
                        // TODO: return the maximum single-transaction profit
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] prices = new int[n];
                        for (int i = 0; i < n; i++) prices[i] = sc.nextInt();
                        System.out.println(new Solution().maxProfit(prices));
                    }
                }
                """;
    }

    private String starterBinarySearch() {
        return """
                import java.util.*;

                class Solution {
                    public int search(int[] nums, int target) {
                        // TODO: return the target index or -1
                        return -1;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int target = sc.nextInt();
                        System.out.println(new Solution().search(nums, target));
                    }
                }
                """;
    }

    private String starterRotateArray() {
        return """
                import java.util.*;

                class Solution {
                    public void rotate(int[] nums, int k) {
                        // TODO: rotate nums in place
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int k = sc.nextInt();
                        new Solution().rotate(nums, k);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < n; i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(nums[i]);
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterProductExceptSelf() {
        return """
                import java.util.*;

                class Solution {
                    public int[] productExceptSelf(int[] nums) {
                        // TODO: return the product array
                        return new int[nums.length];
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int[] ans = new Solution().productExceptSelf(nums);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < ans.length; i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(ans[i]);
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterRangeSumQuery() {
        return """
                import java.util.*;

                class Solution {
                    public int rangeSum(int[] nums, int left, int right) {
                        // TODO: return the sum from left to right inclusive
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int left = sc.nextInt();
                        int right = sc.nextInt();
                        System.out.println(new Solution().rangeSum(nums, left, right));
                    }
                }
                """;
    }

    private String starterMinSizeSubarraySum() {
        return """
                import java.util.*;

                class Solution {
                    public int minSubArrayLen(int target, int[] nums) {
                        // TODO: return the shortest valid window length
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int target = sc.nextInt();
                        System.out.println(new Solution().minSubArrayLen(target, nums));
                    }
                }
                """;
    }

    private String starterReverseString() {
        return """
                import java.util.*;

                class Solution {
                    public String reverseString(String s) {
                        // TODO: return the reversed string
                        return s;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.next();
                        System.out.println(new Solution().reverseString(s));
                    }
                }
                """;
    }

    private String starterValidAnagram() {
        return """
                import java.util.*;

                class Solution {
                    public boolean isAnagram(String s, String t) {
                        // TODO: return true if t is an anagram of s
                        return false;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.next();
                        String t = sc.next();
                        System.out.println(new Solution().isAnagram(s, t));
                    }
                }
                """;
    }

    private String starterLongestCommonPrefix() {
        return """
                import java.util.*;

                class Solution {
                    public String longestCommonPrefix(String[] words) {
                        // TODO: return the longest shared prefix
                        return "";
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int m = sc.nextInt();
                        String[] words = new String[m];
                        for (int i = 0; i < m; i++) words[i] = sc.next();
                        System.out.println(new Solution().longestCommonPrefix(words));
                    }
                }
                """;
    }

    private String starterFirstUniqueCharacter() {
        return """
                import java.util.*;

                class Solution {
                    public int firstUniqChar(String s) {
                        // TODO: return the first unique character index
                        return -1;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.next();
                        System.out.println(new Solution().firstUniqChar(s));
                    }
                }
                """;
    }

    private String starterLongestSubstringWithoutRepeating() {
        return """
                import java.util.*;

                class Solution {
                    public int lengthOfLongestSubstring(String s) {
                        // TODO: return the maximum unique-substring length
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.next();
                        System.out.println(new Solution().lengthOfLongestSubstring(s));
                    }
                }
                """;
    }

    private String starterValidParentheses() {
        return """
                import java.util.*;

                class Solution {
                    public boolean isValid(String s) {
                        // TODO: return true if the bracket string is valid
                        return false;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.next();
                        System.out.println(new Solution().isValid(s));
                    }
                }
                """;
    }

    private String starterNextGreaterElement() {
        return """
                import java.util.*;

                class Solution {
                    public int[] nextGreaterElements(int[] nums) {
                        // TODO: return the next greater value for each index
                        return new int[nums.length];
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int[] ans = new Solution().nextGreaterElements(nums);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < ans.length; i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(ans[i]);
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterDailyTemperatures() {
        return """
                import java.util.*;

                class Solution {
                    public int[] dailyTemperatures(int[] temperatures) {
                        // TODO: return the wait for a warmer day
                        return new int[temperatures.length];
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] temperatures = new int[n];
                        for (int i = 0; i < n; i++) temperatures[i] = sc.nextInt();
                        int[] ans = new Solution().dailyTemperatures(temperatures);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < ans.length; i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(ans[i]);
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterFibonacci() {
        return """
                import java.util.*;

                class Solution {
                    public int fib(int n) {
                        // TODO: return F(n)
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        System.out.println(new Solution().fib(n));
                    }
                }
                """;
    }

    private String starterClimbingStairs() {
        return """
                import java.util.*;

                class Solution {
                    public int climbStairs(int n) {
                        // TODO: return the number of distinct ways
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        System.out.println(new Solution().climbStairs(n));
                    }
                }
                """;
    }

    private String starterMinCostClimbingStairs() {
        return """
                import java.util.*;

                class Solution {
                    public int minCostClimbingStairs(int[] cost) {
                        // TODO: return the minimum cost to reach the top
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] cost = new int[n];
                        for (int i = 0; i < n; i++) cost[i] = sc.nextInt();
                        System.out.println(new Solution().minCostClimbingStairs(cost));
                    }
                }
                """;
    }

    private String starterHouseRobber() {
        return """
                import java.util.*;

                class Solution {
                    public int rob(int[] nums) {
                        // TODO: return the maximum amount you can rob
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        System.out.println(new Solution().rob(nums));
                    }
                }
                """;
    }

    private String starterMatrixDiagonalSum() {
        return """
                import java.util.*;

                class Solution {
                    public int diagonalSum(int[][] matrix) {
                        // TODO: return the diagonal sum
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[][] matrix = new int[n][n];
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {
                                matrix[i][j] = sc.nextInt();
                            }
                        }
                        System.out.println(new Solution().diagonalSum(matrix));
                    }
                }
                """;
    }

    private String starterSearch2DMatrix() {
        return """
                import java.util.*;

                class Solution {
                    public boolean searchMatrix(int[][] matrix, int target) {
                        // TODO: return true if target exists
                        return false;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int rows = sc.nextInt();
                        int cols = sc.nextInt();
                        int[][] matrix = new int[rows][cols];
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < cols; j++) {
                                matrix[i][j] = sc.nextInt();
                            }
                        }
                        int target = sc.nextInt();
                        System.out.println(new Solution().searchMatrix(matrix, target));
                    }
                }
                """;
    }

    private String starterSpiralMatrix() {
        return """
                import java.util.*;

                class Solution {
                    public List<Integer> spiralOrder(int[][] matrix) {
                        // TODO: return the spiral traversal
                        return new ArrayList<>();
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int rows = sc.nextInt();
                        int cols = sc.nextInt();
                        int[][] matrix = new int[rows][cols];
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < cols; j++) {
                                matrix[i][j] = sc.nextInt();
                            }
                        }
                        List<Integer> ans = new Solution().spiralOrder(matrix);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < ans.size(); i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(ans.get(i));
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterReverseLinkedList() {
        return """
                import java.util.*;

                class ListNode {
                    int val;
                    ListNode next;
                    ListNode(int val) { this.val = val; }
                }

                class Solution {
                    public ListNode reverseList(ListNode head) {
                        // TODO: reverse the linked list
                        return head;
                    }
                }

                public class Main {
                    private static ListNode build(int[] nums) {
                        ListNode dummy = new ListNode(0);
                        ListNode tail = dummy;
                        for (int num : nums) {
                            tail.next = new ListNode(num);
                            tail = tail.next;
                        }
                        return dummy.next;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        ListNode head = new Solution().reverseList(build(nums));
                        StringBuilder sb = new StringBuilder();
                        while (head != null) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(head.val);
                            head = head.next;
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterMiddleOfLinkedList() {
        return """
                import java.util.*;

                class ListNode {
                    int val;
                    ListNode next;
                    ListNode(int val) { this.val = val; }
                }

                class Solution {
                    public ListNode middleNode(ListNode head) {
                        // TODO: return the second middle when length is even
                        return head;
                    }
                }

                public class Main {
                    private static ListNode build(int[] nums) {
                        ListNode dummy = new ListNode(0);
                        ListNode tail = dummy;
                        for (int num : nums) {
                            tail.next = new ListNode(num);
                            tail = tail.next;
                        }
                        return dummy.next;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        System.out.println(new Solution().middleNode(build(nums)).val);
                    }
                }
                """;
    }

    private String starterMergeTwoSortedLists() {
        return """
                import java.util.*;

                class ListNode {
                    int val;
                    ListNode next;
                    ListNode(int val) { this.val = val; }
                }

                class Solution {
                    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
                        // TODO: merge the two sorted linked lists
                        return list1;
                    }
                }

                public class Main {
                    private static ListNode build(int[] nums) {
                        ListNode dummy = new ListNode(0);
                        ListNode tail = dummy;
                        for (int num : nums) {
                            tail.next = new ListNode(num);
                            tail = tail.next;
                        }
                        return dummy.next;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] first = new int[n];
                        for (int i = 0; i < n; i++) first[i] = sc.nextInt();
                        int m = sc.nextInt();
                        int[] second = new int[m];
                        for (int i = 0; i < m; i++) second[i] = sc.nextInt();
                        ListNode head = new Solution().mergeTwoLists(build(first), build(second));
                        StringBuilder sb = new StringBuilder();
                        while (head != null) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(head.val);
                            head = head.next;
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterMaximumDepthBinaryTree() {
        return """
                import java.util.*;

                class TreeNode {
                    int val;
                    TreeNode left;
                    TreeNode right;
                    TreeNode(int val) { this.val = val; }
                }

                class Solution {
                    public int maxDepth(TreeNode root) {
                        // TODO: return the maximum depth
                        return 0;
                    }
                }

                public class Main {
                    private static TreeNode build(String[] tokens, int index) {
                        if (index >= tokens.length || tokens[index].equals("null")) return null;
                        TreeNode node = new TreeNode(Integer.parseInt(tokens[index]));
                        node.left = build(tokens, index * 2 + 1);
                        node.right = build(tokens, index * 2 + 2);
                        return node;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        String[] tokens = new String[n];
                        for (int i = 0; i < n; i++) tokens[i] = sc.next();
                        System.out.println(new Solution().maxDepth(build(tokens, 0)));
                    }
                }
                """;
    }

    private String starterBinaryTreeInorderTraversal() {
        return """
                import java.util.*;

                class TreeNode {
                    int val;
                    TreeNode left;
                    TreeNode right;
                    TreeNode(int val) { this.val = val; }
                }

                class Solution {
                    public List<Integer> inorderTraversal(TreeNode root) {
                        // TODO: return the inorder traversal
                        return new ArrayList<>();
                    }
                }

                public class Main {
                    private static TreeNode build(String[] tokens, int index) {
                        if (index >= tokens.length || tokens[index].equals("null")) return null;
                        TreeNode node = new TreeNode(Integer.parseInt(tokens[index]));
                        node.left = build(tokens, index * 2 + 1);
                        node.right = build(tokens, index * 2 + 2);
                        return node;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        String[] tokens = new String[n];
                        for (int i = 0; i < n; i++) tokens[i] = sc.next();
                        List<Integer> ans = new Solution().inorderTraversal(build(tokens, 0));
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < ans.size(); i++) {
                            if (i > 0) sb.append(" ");
                            sb.append(ans.get(i));
                        }
                        System.out.println(sb);
                    }
                }
                """;
    }

    private String starterSameTree() {
        return """
                import java.util.*;

                class TreeNode {
                    int val;
                    TreeNode left;
                    TreeNode right;
                    TreeNode(int val) { this.val = val; }
                }

                class Solution {
                    public boolean isSameTree(TreeNode p, TreeNode q) {
                        // TODO: return true if both trees are identical
                        return false;
                    }
                }

                public class Main {
                    private static TreeNode build(String[] tokens, int index) {
                        if (index >= tokens.length || tokens[index].equals("null")) return null;
                        TreeNode node = new TreeNode(Integer.parseInt(tokens[index]));
                        node.left = build(tokens, index * 2 + 1);
                        node.right = build(tokens, index * 2 + 2);
                        return node;
                    }

                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n1 = sc.nextInt();
                        String[] first = new String[n1];
                        for (int i = 0; i < n1; i++) first[i] = sc.next();
                        int n2 = sc.nextInt();
                        String[] second = new String[n2];
                        for (int i = 0; i < n2; i++) second[i] = sc.next();
                        System.out.println(new Solution().isSameTree(build(first, 0), build(second, 0)));
                    }
                }
                """;
    }

    private String starterLastStoneWeight() {
        return """
                import java.util.*;

                class Solution {
                    public int lastStoneWeight(int[] stones) {
                        // TODO: return the final remaining stone weight
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] stones = new int[n];
                        for (int i = 0; i < n; i++) stones[i] = sc.nextInt();
                        System.out.println(new Solution().lastStoneWeight(stones));
                    }
                }
                """;
    }

    private String starterNumberOfIslands() {
        return """
                import java.util.*;

                class Solution {
                    public int numIslands(char[][] grid) {
                        // TODO: return the island count
                        return 0;
                    }
                }

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int rows = sc.nextInt();
                        int cols = sc.nextInt();
                        char[][] grid = new char[rows][cols];
                        for (int i = 0; i < rows; i++) {
                            String row = sc.next();
                            for (int j = 0; j < cols; j++) {
                                grid[i][j] = row.charAt(j);
                            }
                        }
                        System.out.println(new Solution().numIslands(grid));
                    }
                }
                """;
    }

    private static class GeneratedProblem {
        private final String title;
        private final String description;
        private final String difficulty;
        private final String topic;
        private final String constraints;
        private final String starterCode;
        private final List<SeedTestCase> tests;

        private GeneratedProblem(String title, String description, String difficulty, String topic,
                String constraints, String starterCode, List<SeedTestCase> tests) {
            this.title = title;
            this.description = description;
            this.difficulty = difficulty;
            this.topic = topic;
            this.constraints = constraints;
            this.starterCode = starterCode;
            this.tests = tests;
        }
    }

    private static class SeedTestCase {
        private final String input;
        private final String expectedOutput;
        private final boolean sample;

        private SeedTestCase(String input, String expectedOutput, boolean sample) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.sample = sample;
        }
    }

    @FunctionalInterface
    private interface IntArraySolver {
        int solve(int[] nums);
    }

    @FunctionalInterface
    private interface IntArrayBooleanSolver {
        boolean solve(int[] nums);
    }

    @FunctionalInterface
    private interface IntArrayTransform {
        int[] solve(int[] nums);
    }

    @FunctionalInterface
    private interface StringSolver {
        String solve(String s);
    }

    @FunctionalInterface
    private interface TwoStringBooleanSolver {
        boolean solve(String a, String b);
    }

    @FunctionalInterface
    private interface StringArraySolver {
        String solve(String[] words);
    }

    @FunctionalInterface
    private interface StringIntSolver {
        int solve(String s);
    }

    @FunctionalInterface
    private interface StringBooleanSolver {
        boolean solve(String s);
    }

    @FunctionalInterface
    private interface IntSolver {
        int solve(int value);
    }

    @FunctionalInterface
    private interface MatrixIntSolver {
        int solve(int[][] matrix);
    }

    @FunctionalInterface
    private interface MatrixTargetBooleanSolver {
        boolean solve(int[][] matrix, int target);
    }

    @FunctionalInterface
    private interface MatrixArraySolver {
        int[] solve(int[][] matrix);
    }

    @FunctionalInterface
    private interface TokenTreeIntSolver {
        int solve(String[] tokens);
    }

    @FunctionalInterface
    private interface TokenTreeArraySolver {
        int[] solve(String[] tokens);
    }

    @FunctionalInterface
    private interface GridIntSolver {
        int solve(String[] grid);
    }
}
