package com.codearena.repository;

import com.codearena.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByTopicIgnoreCase(String topic);

    List<Problem> findByStatus(String status);

    List<Problem> findByStatusAndTopicIgnoreCase(String status, String topic);

    List<Problem> findByCreatedByOrderByIdDesc(Long createdBy);
}
