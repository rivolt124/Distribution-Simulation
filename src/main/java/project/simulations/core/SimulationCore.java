package project.simulations.core;

import java.util.ArrayList;
import java.util.List;

public abstract class SimulationCore<F, I> {
    protected final int repetitions;
    protected int counter;
    private boolean stopped;

    private final List<IReplicationListener<I>> listeners = new ArrayList<>();

    public SimulationCore(int repetitions) {
        this.repetitions = repetitions;
        stopped = false;
    }

    public void stop() {
        stopped = true;
    }

    public F simulate() {
        counter = 0;
        beforeSimulation();
        while (counter < repetitions && !stopped) {
            beforeReplication();
            experiment();
            afterReplication();
            ++counter;
            if (shouldEmitIntermediate()) {
                I intermediate = intermediateResult();
                notifyListeners(counter, intermediate);
            }
        }
        return afterSimulation();
    }

    public void addReplicationListener(IReplicationListener<I> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(int replication, I result) {
        for (IReplicationListener<I> listener : listeners) {
            listener.onReplication(replication, result);
        }
    }

    protected abstract boolean shouldEmitIntermediate();
    protected abstract void experiment();
    protected abstract void beforeSimulation();
    protected abstract F afterSimulation();
    protected abstract void beforeReplication();
    protected abstract void afterReplication();
    protected abstract I intermediateResult();
}