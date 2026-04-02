package project.simulations.results;

import java.time.LocalTime;

public record DepartureSimResult(
    int routeId,
    LocalTime optimalStartTime,
    double achievedProbability
) {}