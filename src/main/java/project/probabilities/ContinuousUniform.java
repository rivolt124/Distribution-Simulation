package project.probabilities;

import java.util.Random;

public class ContinuousUniform implements IDistribution {
    private final double min;
    private final double max;
    private final Random random;

    public ContinuousUniform(double min, double max, long seed) {
        this.min = min;
        this.max = max;
        random = new Random(seed);
    }

    @Override
    public double calculate() {
        return random.nextDouble(max - min) + min;
    }
}
