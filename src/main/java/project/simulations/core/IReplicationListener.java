package project.simulations.core;

@FunctionalInterface
public interface IReplicationListener<T> {
    void onReplication(int replicationNumber, T intermediateResult);
}
