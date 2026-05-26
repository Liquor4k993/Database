package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.service.ComputeService;

@RestController
@RequestMapping("/compute")
public class ComputeController {

    private final ComputeService computeService;

    @Autowired
    public ComputeController(ComputeService computeService) {
        this.computeService = computeService;
    }

    @GetMapping("/sum")
    public int getSum() {
        return computeService.calculateSum();
    }

    @GetMapping("/sum-parallel")
    public int getSumParallel() {
        return computeService.calculateSumParallel();
    }
}