package project.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {
    private final List<Integer> xs = new ArrayList<>();
    private final List<Double> ys = new ArrayList<>();

    public void clear() {
        xs.clear();
        ys.clear();
        repaint();
    }

    public void addPoint(int x, double y) {
        xs.add(x);
        ys.add(y);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 30;
        int labelPadding = 30;

        // background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        // draw axes
        g2.setColor(Color.BLACK);
        g2.drawLine(padding + labelPadding, h - padding, w - padding, h - padding); // x-axis
        g2.drawLine(padding + labelPadding, h - padding, padding + labelPadding, padding); // y-axis

        if (ys.isEmpty()) {
            g2.dispose();
            return;
        }
        int n = ys.size();
        double minY = ys.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxY = ys.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        int minX = xs.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxX = xs.stream().mapToInt(Integer::intValue).max().orElse(1);
        if (minY == maxY) {
            // avoid zero range
            minY -= 1;
            maxY += 1;
        }

        // draw y labels
        FontMetrics fm = g2.getFontMetrics();
        int numYTicks = 8;
        for (int i = 0; i <= numYTicks; i++) {
            int y = h - padding - (i * (h - 2 * padding) / numYTicks);
            double labelVal = minY + (maxY - minY) * i / numYTicks;
            int totalSeconds = (int) Math.round(labelVal);
            int mins = Math.floorDiv(totalSeconds, 60);
            int secs = Math.floorMod(totalSeconds, 60);
            String label = String.format("%02d:%02d", mins, secs);
            int labelWidth = fm.stringWidth(label);
            g2.setColor(Color.GRAY);
            g2.drawLine(padding + labelPadding - 5, y, w - padding, y);
            g2.setColor(Color.BLACK);
            g2.drawString(label, padding + labelPadding - labelWidth - 8, y + (fm.getAscent() / 2) - 2);
        }

        // draw polyline
        int plotWidth = w - 2 * padding - labelPadding;
        int plotHeight = h - 2 * padding;
        int prevX = -1, prevY = -1;
        g2.setColor(Color.RED);
        for (int i = 0; i < n; i++) {
            double xNorm = (maxX == minX) ? 0.5 : (double) (xs.get(i) - minX) / (double) (maxX - minX);
            int x = padding + labelPadding + (int) (xNorm * plotWidth);
            double v = ys.get(i);
            double yNorm = (v - minY) / (maxY - minY);
            int y = h - padding - (int) (yNorm * plotHeight);
            g2.fillOval(x - 3, y - 3, 6, 6);
            if (i > 0) {
                g2.drawLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }

        // draw x labels
        g2.setColor(Color.BLACK);
        int desiredLabels = 12;
        int rangeX = Math.max(1, maxX - minX);
        int step = Math.max(1, rangeX / desiredLabels);
        for (int xv = minX; xv <= maxX; xv += step) {
            double xNorm = (maxX == minX) ? 0.5 : (double) (xv - minX) / (double) (maxX - minX);
            int x = padding + labelPadding + (int) (xNorm * plotWidth);
            String lbl = String.valueOf(xv);
            int lblW = fm.stringWidth(lbl);
            g2.drawLine(x, h - padding, x, h - padding + 5);
            g2.drawString(lbl, x - lblW / 2, h - padding + fm.getAscent() + 8);
        }

        g2.dispose();
    }
}
