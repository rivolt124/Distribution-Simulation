import java.util.Random;

import project.probabilities.ContinuousEmpiric;
import project.probabilities.ContinuousUniform;
import project.probabilities.DiscreteEmpiric;
import project.probabilities.DiscreteUniform;

public class PrimaryTester {
    public static void main(String[] args) {
        Random lcg = new Random(123456789);
        int samplesCount = 100_000;

        TestRunner.runAndExport("DiscreteUniform-RED.csv", new DiscreteUniform(55, 75, lcg.nextLong())::calculate, samplesCount);
        TestRunner.runAndExport("ContinuousUniform-GREEN.csv", new ContinuousUniform(50.0, 80.0, lcg.nextLong())::calculate, samplesCount);
        TestRunner.runAndExport("ContinuousEmpiric-BLACK.csv", new ContinuousEmpiric(
                new double[]{75, 10, 45, 32, 20},
                new double[]{85, 20, 75, 45, 32},
                new double[]{0.05, 0.1, 0.15, 0.2, 0.5},
                lcg
        )::calculate, samplesCount);
        TestRunner.runAndExport("DiscreteEmpiric-BLUE.csv", new DiscreteEmpiric(
                new int[]{15, 29, 45},
                new int[]{29, 45, 65},
                new double[]{0.2, 0.4, 0.4},
                lcg
        )::calculate, samplesCount);
    }
}


