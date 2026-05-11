package com.codearena.controller;

import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.service.ProblemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public List<Problem> list(@RequestParam(required = false) String topic) {
        return problemService.findAll(topic);
    }

    @GetMapping("/{id}")
    public Problem get(@PathVariable Long id) {
        return problemService.findById(id);
    }

    @GetMapping("/{id}/samples")
    public List<TestCase> samples(@PathVariable Long id) {
        return problemService.sampleTestCases(id);
    }
}
