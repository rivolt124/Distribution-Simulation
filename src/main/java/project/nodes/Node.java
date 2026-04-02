package project.nodes;

import java.util.ArrayList;
import java.util.List;

public class Node {
    protected final List<RoadSegment> outgoing;
    protected final NodeType type;
    protected final String name;

    public Node(NodeType type, String name) {
        this.type = type;
        this.name = name;
        outgoing = new ArrayList<>();
    }

    public void addSegment(RoadSegment segment) {
        outgoing.add(segment);
    }

    public List<RoadSegment> getOutgoing() {
        return outgoing;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name; 
    }
}
