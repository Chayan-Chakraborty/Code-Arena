package com.codearena.service;

import com.codearena.entity.Problem;
import com.codearena.entity.Submission;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class DashboardService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public DashboardService(SubmissionRepository submissionRepository, ProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    public Map<String, Object> stats(Long userId) {
        List<Submission> allSubs = submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Submission> accepted = submissionRepository.findByUserIdAndStatus(userId, "Accepted");
        Set<Long> solvedIds = new HashSet<>();
        for (Submission s : accepted)
            solvedIds.add(s.getProblemId());

        List<Problem> allProblems = problemRepository.findAll();
        Map<Long, Problem> problemsById = new HashMap<>();
        for (Problem p : allProblems)
            problemsById.put(p.getId(), p);

        Map<String, Integer> byDifficulty = new HashMap<>();
        Map<String, Integer> byTopic = new HashMap<>();
        for (Long pid : solvedIds) {
            Problem p = problemsById.get(pid);
            if (p == null)
                continue;
            byDifficulty.merge(p.getDifficulty(), 1, Integer::sum);
            byTopic.merge(p.getTopic(), 1, Integer::sum);
        }

        // Topic-wise progress: solved / total per topic
        Map<String, int[]> topicProgress = new TreeMap<>();
        Map<String, List<Map<String, Object>>> solvedByTopic = new TreeMap<>();
        for (Problem p : allProblems) {
            int[] arr = topicProgress.computeIfAbsent(p.getTopic(), k -> new int[] { 0, 0 });
            arr[1] += 1;
            if (solvedIds.contains(p.getId())) {
                arr[0] += 1;
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", p.getId());
                entry.put("title", p.getTitle());
                entry.put("difficulty", p.getDifficulty());
                solvedByTopic.computeIfAbsent(p.getTopic(), k -> new ArrayList<>()).add(entry);
            }
        }
        solvedByTopic.values()
                .forEach(list -> list.sort(Comparator.comparing(m -> ((Long) m.get("id")))));

        Map<String, Map<String, Integer>> topicView = new LinkedHashMap<>();
        topicProgress.forEach((k, v) -> {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("solved", v[0]);
            m.put("total", v[1]);
            topicView.put(k, m);
        });

        // Recent submissions (cap at 10)
        List<Map<String, Object>> recent = new ArrayList<>();
        int limit = Math.min(allSubs.size(), 10);
        for (int i = 0; i < limit; i++) {
            Submission s = allSubs.get(i);
            Problem p = problemsById.get(s.getProblemId());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", s.getId());
            entry.put("problemId", s.getProblemId());
            entry.put("problemTitle", p != null ? p.getTitle() : null);
            entry.put("difficulty", p != null ? p.getDifficulty() : null);
            entry.put("status", s.getStatus());
            entry.put("executionTime", s.getExecutionTime());
            entry.put("createdAt", s.getCreatedAt());
            recent.add(entry);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSolved", solvedIds.size());
        result.put("totalProblems", (long) allProblems.size());
        result.put("byDifficulty", byDifficulty);
        result.put("byTopic", byTopic);
        result.put("topicProgress", topicView);
        result.put("solvedByTopic", solvedByTopic);
        result.put("recentSubmissions", recent);
        result.put("totalSubmissions", allSubs.size());
        return result;
    }
}
