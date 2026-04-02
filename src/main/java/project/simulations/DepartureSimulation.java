package project.simulations;

import java.time.LocalTime;

import project.models.CourierModel;
import project.simulations.core.SimulationCore;
import project.simulations.results.CourierSimResult;
import project.simulations.results.DepartureSimResult;
import project.simulations.results.IntermediateResult;

public class DepartureSimulation extends SimulationCore<DepartureSimResult, IntermediateResult>{
    private final double targetPercentage;
    private final double tolerance;
    private final CourierModel model;
    private final int routeId;
    private final LocalTime kSlowTime;
    private final LocalTime deadline;

    private int lowSeconds;
    private int highSeconds;
    private LocalTime candidateStartTime;
    private OnTimeSimulation currentSimulation;
    private double currentProbability;
    private LocalTime bestStart;
    private double bestProbability;

    public DepartureSimulation(double targetPercentage, double tolerance, int repetitions, CourierModel model, int routeId, LocalTime kSlowTime, LocalTime deadline) {
        super(repetitions);
        this.targetPercentage = targetPercentage;
        this.tolerance = tolerance;
        this.model = model;
        this.routeId = routeId;
        this.kSlowTime = kSlowTime;
        this.deadline = deadline;
    }

    @Override
    public void experiment() {
        CourierSimResult result = currentSimulation.simulate();
        currentProbability = result.onTimeProbability();

        if (bestProbability < 0 || Math.abs(currentProbability - targetPercentage) < Math.abs(bestProbability - targetPercentage)) {
            bestStart = candidateStartTime;
            bestProbability = currentProbability;
        }
    }

    @Override
    public void beforeSimulation() {
        lowSeconds = 0;
        highSeconds = deadline.toSecondOfDay();
        bestStart = null;
        bestProbability = -1;
    }

    @Override
    public DepartureSimResult afterSimulation() {
        return new DepartureSimResult(routeId, bestStart, bestProbability);
    }

    @Override
    public void beforeReplication() {
        int mid = (lowSeconds + highSeconds) / 2;
        candidateStartTime = LocalTime.ofSecondOfDay(mid);

        currentSimulation = new OnTimeSimulation(
                repetitions,
                model,
                routeId,
                candidateStartTime,
                kSlowTime,
                deadline
        );
    }

    @Override
    public void afterReplication() {
        if (Math.abs(bestProbability - targetPercentage) <= tolerance) {
            lowSeconds = highSeconds = candidateStartTime.toSecondOfDay();
            // System.out.println("Stopping on replication count: " + counter);
            stop();
        } else if (currentProbability < targetPercentage) {
            highSeconds = candidateStartTime.toSecondOfDay() - 1;
        } else {
            lowSeconds = candidateStartTime.toSecondOfDay() + 1;
        }
    }

    @Override
    protected IntermediateResult intermediateResult() {
        return new IntermediateResult(0, candidateStartTime.toSecondOfDay(), counter);
    }

    @Override
    protected boolean shouldEmitIntermediate() {
        return true;
    }
}
