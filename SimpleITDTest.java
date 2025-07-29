import muse2.*;

public class SimpleITDTest {
    public static void main(String[] args) {
        System.out.println("=== Simple ITD Test ===");
        
        float sampleRate = 44100.0f;
        int delaySamples = 22; // ~500μs at 44.1kHz
        
        // Create simple delayed signals
        float[] left = new float[1000];
        float[] right = new float[1000];
        
        // Fill left with sine wave
        for (int i = 0; i < left.length; i++) {
            left[i] = (float) Math.sin(2 * Math.PI * 100 * i / sampleRate);
        }
        
        // Fill right with delayed version
        for (int i = 0; i < right.length; i++) {
            if (i >= delaySamples) {
                right[i] = left[i - delaySamples];
            }
        }
        
        // Measure ITD
        double measuredITD = SpatialCueAnalyzer.computeITD(left, right, sampleRate);
        double expectedITD = delaySamples * 1_000_000.0 / sampleRate;
        
        System.out.printf("Expected ITD: %.1f μs\n", expectedITD);
        System.out.printf("Measured ITD: %.1f μs\n", measuredITD);
        System.out.printf("Error: %.1f μs\n", Math.abs(measuredITD - expectedITD));
        
        if (Math.abs(measuredITD - expectedITD) < 50.0) {
            System.out.println("✅ ITD measurement working correctly!");
        } else {
            System.out.println("❌ ITD measurement needs fixing");
        }
    }
} 