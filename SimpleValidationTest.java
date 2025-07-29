import java.io.*;

public class SimpleValidationTest {
    
    public static void main(String[] args) {
        System.out.println("🔬 Simple MUSE2 Validation Test");
        System.out.println("===============================");
        
        // Test ITD measurement with known values
        testITDAccuracy();
        
        // Test ILD measurement
        testILDAccuracy();
        
        // Test with noise
        testITDWithNoise();
        
        System.out.println("\n✅ Simple validation completed!");
    }
    
    private static void testITDAccuracy() {
        System.out.println("\n🎯 Testing ITD Measurement Accuracy");
        
        double[] testITDs = {50, 100, 200, 500, 1000}; // microseconds
        double[] frequencies = {500, 1000, 2000, 4000}; // Hz
        float sampleRate = 44100;
        
        int totalTests = 0;
        int passedTests = 0;
        double totalError = 0;
        
        for (double freq : frequencies) {
            for (double expectedITD : testITDs) {
                totalTests++;
                
                // Generate test signal with known ITD
                float[][] stereo = generateTestSignal(freq, expectedITD, sampleRate);
                
                // Test ITD measurement
                double measuredITD = muse2.SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
                double error = Math.abs(measuredITD - expectedITD);
                totalError += error;
                
                boolean pass = error < 50; // Target accuracy
                String result = pass ? "✅ PASS" : "❌ FAIL";
                
                if (pass) passedTests++;
                
                System.out.printf("ITD Test: %.0fHz, Expected: %.1fμs, Measured: %.1fμs, Error: %.1fμs %s\n",
                    freq, expectedITD, measuredITD, error, result);
            }
        }
        
        double passRate = (passedTests * 100.0) / totalTests;
        double avgError = totalError / totalTests;
        
        System.out.printf("\nITD Accuracy: %d/%d tests passed (%.1f%%)\n", passedTests, totalTests, passRate);
        System.out.printf("Average Error: %.1fμs (target <50μs)\n", avgError);
        
        if (avgError < 50) {
            System.out.println("✅ ITD measurement meets research standards!");
        } else {
            System.out.println("❌ ITD measurement needs improvement");
        }
    }
    
    private static void testILDAccuracy() {
        System.out.println("\n📊 Testing ILD Measurement Accuracy");
        
        double[] testILDs = {1, 3, 6, 10, 15}; // dB
        double[] frequencies = {500, 1000, 2000, 4000}; // Hz
        float sampleRate = 44100;
        
        int totalTests = 0;
        int passedTests = 0;
        double totalError = 0;
        
        for (double freq : frequencies) {
            for (double expectedILD : testILDs) {
                totalTests++;
                
                // Generate test signal with known ILD
                float[][] stereo = generateTestSignalWithILD(freq, expectedILD, sampleRate);
                
                // Test ILD measurement
                double measuredILD = muse2.SpatialCueAnalyzer.computeILD(stereo[0], stereo[1]);
                double error = Math.abs(measuredILD - expectedILD);
                totalError += error;
                
                boolean pass = error < 1.0; // Target accuracy
                String result = pass ? "✅ PASS" : "❌ FAIL";
                
                if (pass) passedTests++;
                
                System.out.printf("ILD Test: %.0fHz, Expected: %.1fdB, Measured: %.1fdB, Error: %.1fdB %s\n",
                    freq, expectedILD, measuredILD, error, result);
            }
        }
        
        double passRate = (passedTests * 100.0) / totalTests;
        double avgError = totalError / totalTests;
        
        System.out.printf("\nILD Accuracy: %d/%d tests passed (%.1f%%)\n", passedTests, totalTests, passRate);
        System.out.printf("Average Error: %.1fdB (target <1dB)\n", avgError);
    }
    
    private static void testITDWithNoise() {
        System.out.println("\n🔊 Testing ITD Measurement with Noise");
        
        double expectedITD = 250.0; // microseconds
        float sampleRate = 44100;
        double[] noiseLevels = {0.0, 0.1, 0.2, 0.3}; // 0%, 10%, 20%, 30% noise
        
        for (double noiseLevel : noiseLevels) {
            // Generate signal with known ITD plus noise
            float[][] stereo = generateSignalWithNoise(1000.0, expectedITD, noiseLevel, sampleRate);
            
            // Test analyzer accuracy with noisy signals
            double measuredITD = muse2.SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
            double error = Math.abs(measuredITD - expectedITD);
            
            boolean pass = error < 50.0;
            String result = pass ? "✅ PASS" : "❌ FAIL";
            
            System.out.printf("Noise Test: %.0f%% noise, Expected: %.1fμs, Measured: %.1fμs, Error: %.1fμs %s\n",
                noiseLevel * 100, expectedITD, measuredITD, error, result);
        }
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
    
    private static float[][] generateTestSignalWithILD(double frequency, double ild, float sampleRate) {
        float duration = 1.0f; // 1 second
        int numSamples = (int) (sampleRate * duration);
        
        float[] left = new float[numSamples];
        float[] right = new float[numSamples];
        
        // Generate base signal
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            float sample = (float) Math.sin(2 * Math.PI * frequency * t);
            left[i] = sample;
            right[i] = sample;
        }
        
        // Apply ILD (level difference)
        double ildLinear = Math.pow(10, ild / 20);
        for (int i = 0; i < numSamples; i++) {
            right[i] = (float) (right[i] / ildLinear);
        }
        
        return new float[][] { left, right };
    }
    
    private static float[][] generateSignalWithNoise(double frequency, double itd, double noiseLevel, float sampleRate) {
        float duration = 1.0f; // 1 second
        int numSamples = (int) (sampleRate * duration);
        
        float[] left = new float[numSamples];
        float[] right = new float[numSamples];
        
        // Generate base signal with noise
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            double signal = Math.sin(2 * Math.PI * frequency * t);
            double noise = (Math.random() - 0.5) * 2 * noiseLevel;
            left[i] = (float) (signal + noise);
        }
        
        // Apply ITD with noise
        int itdSamples = (int) (itd * sampleRate / 1_000_000);
        
        if (itdSamples >= 0) {
            // Right ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0) {
                    double signal = left[j];
                    double noise = (Math.random() - 0.5) * 2 * noiseLevel;
                    right[i] = (float) (signal + noise);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0) {
                    double signal = left[j];
                    double noise = (Math.random() - 0.5) * 2 * noiseLevel;
                    right[i] = (float) (signal + noise);
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
} 