package project.probabilities;

import java.util.Random;

public class DiscreteEmpiric implements IDistribution {
    private final int[] min;
    private final int[] max;
    private final double[] probabilities;
    private final Random random;
    private final Random[] randoms;

    public DiscreteEmpiric(int[] min, int[] max, double[] probabilities, Random linearConcurentGenerator) {
        this.min = min;
        this.max = max;
        this.probabilities = probabilities;
        random = new Random(linearConcurentGenerator.nextLong());
        randoms = new Random[probabilities.length];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = new Random(linearConcurentGenerator.nextLong());
        }
    }

    @Override
    public double calculate() {
        double p = random.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (p < cumulative) {
                return randoms[i].nextInt(max[i] - min[i]) + min[i];
            }
        }
        throw new ArithmeticException("Calculation failed.");
    }
}
