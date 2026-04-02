import java.io.FileWriter;
import java.io.IOException;
import java.util.function.DoubleSupplier;

public class TestRunner {
    public static void runAndExport(String fileName, DoubleSupplier supplier, int samplesCount) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("value\n");

            for (int i = 0; i < samplesCount; i++) {
                double value = supplier.getAsDouble();
                writer.write(value + "\n");
            }

            System.out.println("Exported: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to export " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
