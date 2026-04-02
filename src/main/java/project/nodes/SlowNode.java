package project.nodes;

import project.probabilities.IDistribution;

public class SlowNode extends Node {
    private final IDistribution kDistribution;

    public SlowNode(NodeType type, String name, IDistribution kDistribution) {
        super(type, name);
        this.kDistribution = kDistribution;
    }

    public IDistribution getDistribution() {
        return kDistribution;
    }
}
