package com.codearena.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(columnDefinition = "TEXT")
    private String code;

    private String status; // Accepted, Wrong Answer, TLE, Runtime Error, Compilation Error

    @Column(name = "execution_time")
    private Double executionTime;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
    }
}
