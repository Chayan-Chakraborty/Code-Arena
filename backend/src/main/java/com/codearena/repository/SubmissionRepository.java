package com.codearena.repository;

import com.codearena.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByProblemIdOrderByCreatedAtDesc(Long problemId);

    List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Submission> findByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);

    List<Submission> findByUserIdAndStatus(Long userId, String status);
}
