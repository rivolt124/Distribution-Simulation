package project.simulations;


import java.time.LocalTime;

import project.models.CourierModel;
import project.simulations.results.CourierSimResult;

public class OnTimeSimulation extends CourierSimulation {
    private final LocalTime deadline;
    private int onTimeArrivals;

    public OnTimeSimulation(int repetitions, CourierModel model, int routeId, LocalTime startTime, LocalTime kSlowTime, LocalTime deadline) {
        super(repetitions, model, routeId, startTime, kSlowTime);
        this.deadline = deadline;
    }


    @Override
    public void beforeSimulation() {
        super.beforeSimulation();
        onTimeArrivals = 0;
    }

    @Override
    public CourierSimResult afterSimulation() {
        double percentOnTime = (double) onTimeArrivals / counter;
        return new CourierSimResult(routeId, startTime, null, percentOnTime);
    }

    @Override
    public void afterReplication() {
        super.afterReplication();
        if (startTime.toSecondOfDay() + currentTotalTime < deadline.toSecondOfDay()) {
            ++onTimeArrivals;
        }
    }
}
