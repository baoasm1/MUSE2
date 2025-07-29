import java.io.*;

public class QuickValidationTest {
    
    public static void main(String[] args) {
        System.out.println("🔬 Quick MUSE2 Validation Test");
        System.out.println("==============================");
        
        // Test ITD measurement improvement
        testITDAccuracy();
        
        // Test system readiness
        testSystemReadiness();
        
        System.out.println("\n✅ Quick validation completed!");
    }
    
    private static void testITDAccuracy() {
        System.out.println("\n🎯 Testing ITD Measurement Accuracy");
        
        // Generate test signals with known ITD values
        double[] testITDs = {50, 100, 200, 500, 1000}; // microseconds
        double[] frequencies = {1000, 2000}; // Hz
        float sampleRate = 44100;
        
        int totalTests = 0;
        int passedTests = 0;
        
        for (double freq : frequencies) {
            for (double expectedITD : testITDs) {
                totalTests++;
                
                // Generate synthetic stereo signal
                float[][] stereo = generateTestSignal(freq, expectedITD, sampleRate);
                
                // Test ITD measurement (simplified)
                double measuredITD = measureITD(stereo[0], stereo[1], sampleRate);
                double error = Math.abs(measuredITD - expectedITD);
                
                boolean pass = error < 50; // Target accuracy
                String result = pass ? "✅ PASS" : "❌ FAIL";
                
                if (pass) passedTests++;
                
                System.out.printf("ITD Test: %.0fHz, Expected: %.1fμs, Measured: %.1fμs, Error: %.1fμs %s\n",
                    freq, expectedITD, measuredITD, error, result);
            }
        }
        
        double passRate = (passedTests * 100.0) / totalTests;
        System.out.printf("\nITD Accuracy: %d/%d tests passed (%.1f%%)\n", passedTests, totalTests, passRate);
    }
    
    private static void testSystemReadiness() {
        System.out.println("\n🔧 Testing System Readiness");
        
        // Check for required directories
        String[] requiredDirs = {"input_wavs", "output_stimuli", "bin"};
        int existingDirs = 0;
        
        for (String dir : requiredDirs) {
            if (new File(dir).exists()) {
                existingDirs++;
                System.out.printf("✅ Directory exists: %s\n", dir);
            } else {
                System.out.printf("❌ Missing directory: %s\n", dir);
            }
        }
        
        // Check for compiled classes
        String[] requiredClasses = {"bin/muse2/SpatialCueAnalyzer.class", "bin/muse2/AudioProcessor.class"};
        int existingClasses = 0;
        
        for (String cls : requiredClasses) {
            if (new File(cls).exists()) {
                existingClasses++;
                System.out.printf("✅ Class compiled: %s\n", cls);
            } else {
                System.out.printf("❌ Missing class: %s\n", cls);
            }
        }
        
        double readinessScore = ((existingDirs + existingClasses) * 100.0) / (requiredDirs.length + requiredClasses.length);
        System.out.printf("\nSystem Readiness: %.1f%%\n", readinessScore);
    }
    
    private static float[][] generateTestSignal(double frequency, double itd, float sampleRate) {
        float duration = 1.0f; // 1 second
        int numSamples = (int) (sampleRate * duration);
        
        float[] left = new float[numSamples];
        float[] right = new float[numSamples];
        
        // Generate base signal
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            left[i] = (float) Math.sin(2 * Math.PI * frequency * t);
        }
        
        // Apply ITD (delay to right channel)
        int itdSamples = (int) (itd * sampleRate / 1_000_000);
        
        if (itdSamples >= 0) {
            // Right ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0) {
                    right[i] = left[j];
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0) {
                    right[i] = left[j];
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
    
    private static double measureITD(float[] left, float[] right, float sampleRate) {
        // Simplified cross-correlation for ITD measurement
        int maxLag = (int)(sampleRate * 0.001); // +/-1ms window
        int bestLag = 0;
        double maxCorr = Double.NEGATIVE_INFINITY;
        
        for (int lag = -maxLag; lag <= maxLag; lag++) {
            double corr = crossCorrelation(left, right, lag);
            if (corr > maxCorr) {
                maxCorr = corr;
                bestLag = lag;
            }
        }
        
        // Convert lag (samples) to microseconds
        return bestLag * 1_000_000.0 / sampleRate;
    }
    
    private static double crossCorrelation(float[] x, float[] y, int lag) {
        int len = Math.min(x.length, y.length);
        double sum = 0;
        
        if (lag >= 0) {
            for (int i = 0; i < len - lag; i++) {
                sum += x[i] * y[i + lag];
            }
        } else {
            for (int i = -lag; i < len; i++) {
                sum += x[i] * y[i + lag];
            }
        }
        
        return sum;
    }
} 