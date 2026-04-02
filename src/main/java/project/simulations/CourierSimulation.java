package project.simulations;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import project.models.CourierModel;
import project.nodes.Node;
import project.nodes.NodeType;
import project.nodes.RoadSegment;
import project.nodes.SlowNode;
import project.simulations.core.SimulationCore;
import project.simulations.results.CourierSimResult;
import project.simulations.results.IntermediateResult;

public class CourierSimulation extends SimulationCore<CourierSimResult, IntermediateResult> {
    private static final int TIME_FORMAT = 3600;
    protected final CourierModel model;
    protected final int routeId;
    protected final LocalTime startTime;
    protected final LocalTime kSlowTime;
    protected List<Node> route;
    protected double globalTimeSum;
    protected double currentTotalTime;

    public CourierSimulation(int repetitions, CourierModel model, int routeId, LocalTime startTime, LocalTime kSlowTime) {
        super(repetitions);
        this.model = model;
        this.routeId = routeId;
        this.startTime = startTime;
        this.kSlowTime = kSlowTime;
    }

    @Override
    public void experiment() {
        Node current = route.getFirst();
        for (int i = 1; i < route.size(); i++) {
            Node destination = route.get(i);
            double currentTime = startTime.toSecondOfDay() + currentTotalTime;
            currentTotalTime += travel(current, destination, currentTime);
            current = destination;
        }
    }

    @Override
    public void beforeSimulation() {
        globalTimeSum = 0;
        route = model.getRoute(routeId);
    }

    @Override
    public CourierSimResult afterSimulation() {
        int avgSeconds = (int) Math.round(globalTimeSum / counter);
        LocalTime averageArrival = startTime.plusSeconds(avgSeconds);

        return new CourierSimResult(routeId, startTime, averageArrival, -1);
    }

    @Override
    public void beforeReplication() {
        currentTotalTime = 0;
    }

    @Override
    public void afterReplication() {
        globalTimeSum += currentTotalTime;
    }

    private double travel(Node from, Node to, double currStartTime) {
        Map<Node, Double> timeMap = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(timeMap::get));
        timeMap.put(from, 0.0);
        queue.add(from);
        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            if (curr.equals(to)) {
                return timeMap.get(curr);
            }
            double currTravelTime = timeMap.get(curr);
            double departureTime = currStartTime + currTravelTime;
            for (RoadSegment segment : curr.getOutgoing()) {
                Node dest = model.getDestination(curr, segment);
                if (dest.getType() == NodeType.CITY && !dest.equals(to)) {
                    continue;
                }
                double speed = segment.getDistribution().calculate();
                if (departureTime >= kSlowTime.toSecondOfDay() && curr instanceof SlowNode slowNode ) {
                    speed *= (100 - slowNode.getDistribution().calculate()) / 100.0;
                }
                double time = TIME_FORMAT * segment.getDistance() / speed;
                double newTime = currTravelTime + time;
                if (!timeMap.containsKey(dest) || newTime < timeMap.get(dest)) {
                    timeMap.put(dest, newTime);
                    queue.add(dest);
                }
            }
        }
        return timeMap.getOrDefault(to, Double.POSITIVE_INFINITY);
    }

    @Override
    protected IntermediateResult intermediateResult() {
        double avgSoFar = counter == 0 ? 0.0 : (globalTimeSum / (double) counter);
        return new IntermediateResult(avgSoFar, startTime.toSecondOfDay() + avgSoFar, counter);
    }

    @Override
    protected boolean shouldEmitIntermediate() {
        return counter > 10_000 && counter % 10_000 == 0;
    }
}