package project.simulations.results;

import java.time.LocalTime;

public record CourierSimResult (
    int routeId,
    LocalTime startTime,
    LocalTime averageArrivalTime,
    double onTimeProbability
) {}