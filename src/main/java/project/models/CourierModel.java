package project.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import project.nodes.Node;
import project.nodes.NodeType;
import project.nodes.RoadSegment;
import project.nodes.SlowNode;
import project.probabilities.ContinuousEmpiric;
import project.probabilities.ContinuousUniform;
import project.probabilities.DiscreteEmpiric;
import project.probabilities.DiscreteUniform;
import project.probabilities.IDistribution;

public class CourierModel {
    // LinearConcurentGenerator
    private Random LCG;
    private final Map<Integer, List<Node>> routes;

    public CourierModel(long seed) {
        LCG = new Random(seed);
        routes = new HashMap<>();
    }

    public CourierModel() {
        LCG = new Random();
        routes = new HashMap<>();
    }

    public void updateGenerator(long newSeed) {
        LCG = new Random(newSeed);
    }

    public List<Node> getRoute(int id) {
        return routes.get(id);
    }

    public void initialize() {        
        IDistribution red = new DiscreteUniform(55, 75, LCG.nextLong());
        IDistribution green = new ContinuousUniform(50, 80, LCG.nextLong());
        IDistribution black = new ContinuousEmpiric(
                new double[]{75, 10, 45, 32, 20},
                new double[]{85, 20, 75, 45, 32},
                new double[]{0.05, 0.1, 0.15, 0.2, 0.5},
                LCG
        );
        IDistribution blue = new DiscreteEmpiric(
                new int[]{15, 29, 45},
                new int[]{29, 45, 65},
                new double[]{0.2, 0.4, 0.4},
                LCG
        );
        IDistribution kDistribution = new ContinuousUniform(10, 25, LCG.nextLong());

        Node zilina = new Node(NodeType.CITY, "Z");
        Node divinka = new Node(NodeType.CITY, "D");
        Node strecno = new Node(NodeType.CITY, "S");
        Node rajec = new Node(NodeType.CITY, "R");
        SlowNode k = new SlowNode(NodeType.JUNCTION, "K", kDistribution);
        Node j1 = new Node(NodeType.JUNCTION, "J1");
        Node j2 = new Node(NodeType.JUNCTION, "J2");
        Node j3 = new Node(NodeType.JUNCTION, "J3");
        Node j4 = new Node(NodeType.JUNCTION, "J4");
        Node j5 = new Node(NodeType.JUNCTION, "J5");
        Node j6 = new Node(NodeType.JUNCTION, "J6");

        connect(zilina, divinka, red, 4);
        connect(zilina, divinka, green, 4);
        connect(zilina, k, black, 2);
        connect(zilina, j2, green, 4);
        connect(zilina, j1, red, 3);
        connect(divinka, k, red, 2);
        connect(divinka, j4, black, 1);
        connect(j4, j6, blue, 1);
        connect(j4, j5, red, 3);
        connect(j6, j5, red, 2);
        connect(rajec, j5, blue, 1);
        connect(rajec, k, green, 2);
        connect(rajec, j3, blue, 8);
        connect(strecno, k, blue, 4);
        connect(strecno, j3, blue, 5);
        connect(strecno, j3, black, 5);
        connect(strecno, j2, black, 3);
        connect(strecno, j1, red, 4);

        List<Node> cities = new ArrayList<>();
        cities.add(zilina);
        cities.add(divinka);
        cities.add(strecno);
        cities.add(rajec);

        generateRoutes(zilina, cities);
    }

    private void connect(Node from, Node to, IDistribution distribution, int distance) {
        RoadSegment segment = new RoadSegment(to, from, distribution, distance);
        from.addSegment(segment);
        to.addSegment(segment);
    }

    public Node getDestination(Node node, RoadSegment segment) {
        return segment.getFrom().equals(node) ? segment.getTo() : segment.getFrom();
        // if (segment.getFrom().equals(node))
        //     return segment.getTo();
        // else
        //     return segment.getFrom();
    }

    private void generateRoutes(Node start, List<Node> cities) {
        List<Node> middleCities = new ArrayList<>(cities);
        AtomicInteger routeId = new AtomicInteger(0);
        middleCities.remove(start);
        permute(middleCities, start, routeId, 0);
    }

    private void permute(List<Node> cities, Node start, AtomicInteger routeId, int index) {
        if (index == cities.size()) {
            List<Node> route = new ArrayList<>();
            route.add(start);
            route.addAll(cities);
            route.add(start);
            routes.put(routeId.getAndIncrement(), route);
            return;
        }
        for (int i = index; i < cities.size(); i++) {
            Collections.swap(cities, i, index);
            permute(cities, start, routeId, index + 1);
            Collections.swap(cities, i, index);
        }
    }
}
