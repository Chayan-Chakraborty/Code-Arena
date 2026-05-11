package com.codearena.service;

import com.codearena.entity.Problem;
import com.codearena.entity.Submission;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public DashboardService(SubmissionRepository submissionRepository, ProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    public Map<String, Object> stats(Long userId) {
        List<Submission> accepted = submissionRepository.findByUserIdAndStatus(userId, "Accepted");
        Set<Long> solvedIds = new HashSet<>();
        for (Submission s : accepted)
            solvedIds.add(s.getProblemId());

        List<Problem> solved = problemRepository.findAllById(solvedIds);

        Map<String, Integer> byDifficulty = new HashMap<>();
        Map<String, Integer> byTopic = new HashMap<>();
        for (Problem p : solved) {
            byDifficulty.merge(p.getDifficulty(), 1, Integer::sum);
            byTopic.merge(p.getTopic(), 1, Integer::sum);
        }

        // Topic-wise progress: solved / total per topic
        Map<String, int[]> topicProgress = new HashMap<>();
        for (Problem p : problemRepository.findAll()) {
            int[] arr = topicProgress.computeIfAbsent(p.getTopic(), k -> new int[] { 0, 0 });
            arr[1] += 1;
            if (solvedIds.contains(p.getId()))
                arr[0] += 1;
        }
        Map<String, Map<String, Integer>> topicView = new HashMap<>();
        topicProgress.forEach((k, v) -> {
            Map<String, Integer> m = new HashMap<>();
            m.put("solved", v[0]);
            m.put("total", v[1]);
            topicView.put(k, m);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("totalSolved", solvedIds.size());
        result.put("totalProblems", problemRepository.count());
        result.put("byDifficulty", byDifficulty);
        result.put("byTopic", byTopic);
        result.put("topicProgress", topicView);
        result.put("totalSubmissions", submissionRepository.findByUserIdOrderByCreatedAtDesc(userId).size());
        return result;
    }
}
