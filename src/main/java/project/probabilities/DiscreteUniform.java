package project.probabilities;

import java.util.Random;

public class DiscreteUniform implements IDistribution {
    private final int min;
    private final int max;
    private final Random random;

    public DiscreteUniform(int min, int max, long seed) {
        this.min = min;
        this.max = max;
        random = new Random(seed);
    }

    @Override
    public double calculate() {
        return random.nextInt(max - min + 1) + min;
    }
}
