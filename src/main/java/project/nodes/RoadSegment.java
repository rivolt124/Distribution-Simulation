package project.nodes;

import project.probabilities.IDistribution;

public class RoadSegment {
    protected final Node to;
    protected final Node from;
    protected final IDistribution distribution;
    protected final int distance;

    public RoadSegment(Node to, Node from, IDistribution distribution, int distance) {
        this.to = to;
        this.from = from;
        this.distribution = distribution;
        this.distance = distance;
    }

    public Node getTo() {
        return to;
    }

    public Node getFrom() {
        return from;
    }

    public IDistribution getDistribution() {
        return distribution;
    }

    public int getDistance() {
        return distance;
    }
}
