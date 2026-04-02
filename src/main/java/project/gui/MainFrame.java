package project.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import project.models.CourierModel;
import project.simulations.CourierSimulation;
import project.simulations.DepartureSimulation;
import project.simulations.core.SimulationCore;
import project.simulations.results.CourierSimResult;
import project.simulations.results.DepartureSimResult;
import project.simulations.results.IntermediateResult;

public class MainFrame extends JFrame {
    private final GraphPanel graphPanel = new GraphPanel();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimulationCore<?, ?> currentSimulation;

    public MainFrame() {
        super("Simulation GUI");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 950);
        setResizable(true);

        JPanel top = new JPanel(new GridLayout(0, 2, 8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JComboBox<String> simType = new JComboBox<>(new String[] {"CourierSimulation", "DepartureSimulation"});
        top.add(new JLabel("Simulation type:"));
        top.add(simType);

        JTextField repetitionsField = new JTextField("100000");
        top.add(new JLabel("Repetitions (int):"));
        top.add(repetitionsField);

        JTextField seedField = new JTextField("123456789");
        JCheckBox randomSeed = new JCheckBox("Randomize");
        JPanel seedPanel = new JPanel(new BorderLayout());
        seedPanel.add(seedField, BorderLayout.CENTER);
        seedPanel.add(randomSeed, BorderLayout.EAST);
        top.add(new JLabel("Seed (optional long):"));
        top.add(seedPanel);

        randomSeed.addActionListener(ae -> seedField.setEnabled(!randomSeed.isSelected()));

        // build a temporary model to discover route ids and build human-readable labels
        CourierModel tmp = new CourierModel();
        tmp.initialize();
        class RouteItem {
            final int id;
            final String label;
            RouteItem(int id, String label) { this.id = id; this.label = label; }
            public int getId() { return id; }
            @Override public String toString() { return label; }
        }
        List<RouteItem> routeItems = new ArrayList<>();
        int idx = 0;
        while (tmp.getRoute(idx) != null) {
            List<project.nodes.Node> nodes = tmp.getRoute(idx);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < nodes.size(); j++) {
                if (j > 0) sb.append(" -> ");
                sb.append(nodes.get(j).toString());
            }
            routeItems.add(new RouteItem(idx, sb.toString()));
            idx++;
        }
        JComboBox<RouteItem> routeBox = new JComboBox<>(routeItems.toArray(RouteItem[]::new));
        top.add(new JLabel("Route:"));
        top.add(routeBox);

        JTextField startTimeField = new JTextField("06:00");
        top.add(new JLabel("Start time (HH:mm):"));
        top.add(startTimeField);

        JTextField kSlowField = new JTextField("06:30");
        top.add(new JLabel("K-slow time (HH:mm):"));
        top.add(kSlowField);

        JLabel targetLabel = new JLabel("Target probability (Departure only, e.g. 0.8):");
        JTextField targetField = new JTextField("0.80");
        top.add(targetLabel);
        top.add(targetField);

        JLabel tolLabel = new JLabel("Tolerance (Departure only):");
        JTextField tolField = new JTextField("0.005");
        top.add(tolLabel);
        top.add(tolField);

        JLabel deadlineLabel = new JLabel("Deadline (Departure only HH:mm):");
        JTextField deadlineField = new JTextField("07:35");
        top.add(deadlineLabel);
        top.add(deadlineField);

        JButton stopButton = new JButton("Stop");
        JButton runButton = new JButton("Run");
        // place Stop button in left column and Run in right column
        top.add(stopButton);
        top.add(runButton);

        // initially hide departure-only controls
        targetLabel.setVisible(false);
        targetField.setVisible(false);
        tolLabel.setVisible(false);
        tolField.setVisible(false);
        deadlineLabel.setVisible(false);
        deadlineField.setVisible(false);

        // toggle visibility when simulation type changes
        simType.addActionListener(ae -> {
            boolean isDeparture = "DepartureSimulation".equals((String) simType.getSelectedItem());
            targetLabel.setVisible(isDeparture);
            targetField.setVisible(isDeparture);
            tolLabel.setVisible(isDeparture);
            tolField.setVisible(isDeparture);
            deadlineLabel.setVisible(isDeparture);
            deadlineField.setVisible(isDeparture);
            top.revalidate();
            top.repaint();
        });

        // left panel: final result area in center, graph below
        JPanel leftPanel = new JPanel(new BorderLayout());
        JTextArea finalResultArea = new JTextArea(3, 20);
        finalResultArea.setEditable(false);
        finalResultArea.setLineWrap(true);
        finalResultArea.setWrapStyleWord(true);
        finalResultArea.setFont(new Font("SansSerif", Font.BOLD, 16));
        finalResultArea.setBorder(BorderFactory.createTitledBorder("Final Result"));
        finalResultArea.setBackground(new Color(0xFFFFE0));
        finalResultArea.setOpaque(true);
        finalResultArea.setPreferredSize(new Dimension(900, 80));
        leftPanel.add(finalResultArea, BorderLayout.CENTER);
        graphPanel.setPreferredSize(new Dimension(900, 520));
        leftPanel.add(graphPanel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.CENTER);

        stopButton.addActionListener(e -> {
            if (currentSimulation != null) {
                currentSimulation.stop();
            }
        });

        runButton.addActionListener((ActionEvent e) -> {
            finalResultArea.setText("");
            try {
                int reps = Integer.parseInt(repetitionsField.getText().trim());
                int routeId = ((RouteItem) routeBox.getSelectedItem()).getId();
                LocalTime startTime = LocalTime.parse(startTimeField.getText().trim());
                LocalTime kSlow = LocalTime.parse(kSlowField.getText().trim());

                graphPanel.clear();

                if ("CourierSimulation".equals((String) simType.getSelectedItem())) {
                    CourierModel model;
                    if (randomSeed.isSelected()) {
                        long seed = ThreadLocalRandom.current().nextLong();
                        model = new CourierModel(seed);
                    } else if (seedField.getText().trim().isEmpty()) {
                        model = new CourierModel();
                    } else {
                        long seed = Long.parseLong(seedField.getText().trim());
                        model = new CourierModel(seed);
                    }
                    model.initialize();
                    currentSimulation  = new CourierSimulation(reps, model, routeId, startTime, kSlow);
                    CourierSimulation sim = (CourierSimulation) currentSimulation;
                    sim.addReplicationListener((int replicationNumber, IntermediateResult intermediateResult) -> {
                        double val = intermediateResult.intermedTravelTime();
                        SwingUtilities.invokeLater(() -> {
                            int rep = intermediateResult.repCount();
                            graphPanel.addPoint(rep, val);
                        });
                    });
                    executor.submit(() -> {
                        CourierSimResult result = sim.simulate();
                        SwingUtilities.invokeLater(() -> {
                            finalResultArea.setText("Route: " + result.routeId() + "  Avg arrival: " + result.averageArrivalTime());
                        });
                    });
                } else {
                    double target = Double.parseDouble(targetField.getText().trim());
                    double tol = Double.parseDouble(tolField.getText().trim());
                    LocalTime deadline = LocalTime.parse(deadlineField.getText().trim());
                    CourierModel model;
                    if (randomSeed.isSelected()) {
                        long seed = ThreadLocalRandom.current().nextLong();
                        model = new CourierModel(seed);
                    } else if (seedField.getText().trim().isEmpty()) {
                        model = new CourierModel();
                    } else {
                        long seed = Long.parseLong(seedField.getText().trim());
                        model = new CourierModel(seed);
                    }
                    model.initialize();
                    currentSimulation = new DepartureSimulation(target, tol, reps, model, routeId, kSlow, deadline);
                    DepartureSimulation sim = (DepartureSimulation) currentSimulation;
                    sim.addReplicationListener((int replicationNumber, IntermediateResult intermediateResult) -> {
                        double val = intermediateResult.intermedArrivalTime();
                        SwingUtilities.invokeLater(() -> {
                            int rep = intermediateResult.repCount();
                            graphPanel.addPoint(rep, val / 60);
                        });
                    });
                    executor.submit(() -> {
                        DepartureSimResult result = sim.simulate();
                        SwingUtilities.invokeLater(() -> {
                            finalResultArea.setText("Route: " + result.routeId() + "  Optimal start: " + result.optimalStartTime() + "  Achieved: " + result.achievedProbability());
                            // graphPanel.addPoint(result.achievedProbability() * 100.0);
                        });
                    });
                }
            } catch (NumberFormatException ex) {}
        });
    }
}