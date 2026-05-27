package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class ComputeService {

    private static final Logger logger = LoggerFactory.getLogger(ComputeService.class);

    public int calculateSum() {
        logger.info("Was invoked method for calculate sum (sequential)");

        long startTime = System.currentTimeMillis();

        int sum = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, (a, b) -> a + b);

        long endTime = System.currentTimeMillis();
        logger.info("Sequential sum calculated in {} ms", endTime - startTime);

        return sum;
    }

    public int calculateSumParallel() {
        logger.info("Was invoked method for calculate sum (parallel)");

        long startTime = System.currentTimeMillis();

        int sum = Stream.iterate(1, a -> a + 1)
                .parallel()
                .limit(1_000_000)
                .reduce(0, (a, b) -> a + b);

        long endTime = System.currentTimeMillis();
        logger.info("Parallel sum calculated in {} ms", endTime - startTime);

        return sum;
    }
}