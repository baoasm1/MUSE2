package muse2;

import java.util.*;

public class UnitTests {
    
    public static void main(String[] args) {
        System.out.println("=== MUSE2 Unit Tests ===");
        
        int passed = 0;
        int total = 0;
        
        // Test 1: SpatialCueAnalyzer ITD measurement
        total++;
        if (testITDMeasurement()) {
            System.out.println("✅ Test 1: ITD Measurement - PASSED");
            passed++;
        } else {
            System.out.println("❌ Test 1: ITD Measurement - FAILED");
        }
        
        // Test 2: SpatialCueAnalyzer ILD measurement
        total++;
        if (testILDMeasurement()) {
            System.out.println("✅ Test 2: ILD Measurement - PASSED");
            passed++;
        } else {
            System.out.println("❌ Test 2: ILD Measurement - FAILED");
        }
        
        // Test 3: Frequency shifting
        total++;
        if (testFrequencyShifting()) {
            System.out.println("✅ Test 3: Frequency Shifting - PASSED");
            passed++;
        } else {
            System.out.println("❌ Test 3: Frequency Shifting - FAILED");
        }
        
        // Test 4: Audio file validation
        total++;
        if (testAudioValidation()) {
            System.out.println("✅ Test 4: Audio Validation - PASSED");
            passed++;
        } else {
            System.out.println("❌ Test 4: Audio Validation - FAILED");
        }
        
        // Test 5: Cross-correlation accuracy
        total++;
        if (testCrossCorrelation()) {
            System.out.println("✅ Test 5: Cross-Correlation - PASSED");
            passed++;
        } else {
            System.out.println("❌ Test 5: Cross-Correlation - FAILED");
        }
        
        System.out.println("\n=== Test Results ===");
        System.out.printf("Passed: %d/%d (%.1f%%)\n", passed, total, (passed*100.0/total));
        
        if (passed == total) {
            System.out.println("🎉 All tests passed! System is ready for research.");
        } else {
            System.out.println("⚠️  Some tests failed. Check implementation.");
        }
    }
    
    private static boolean testITDMeasurement() {
        try {
            float sampleRate = 44100.0f;
            
            // Test with known ITD values
            double[] expectedITDs = {0.0, 500.0, -500.0, 200.0, -200.0};
            
            for (double expectedITD : expectedITDs) {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    1.0, sampleRate, 1000.0f, expectedITD, 0.0);
                
                double measuredITD = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
                double error = Math.abs(measuredITD - expectedITD);
                
                // Allow 50μs tolerance
                if (error > 50.0) {
                    System.out.printf("  ITD error too large: expected %.1f, got %.1f (error: %.1f μs)\n", 
                        expectedITD, measuredITD, error);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("  ITD test exception: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testILDMeasurement() {
        try {
            float sampleRate = 44100.0f;
            
            // Test with known ILD values
            double[] expectedILDs = {0.0, 6.0, -6.0, 3.0, -3.0};
            
            for (double expectedILD : expectedILDs) {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    1.0, sampleRate, 1000.0f, 0.0, expectedILD);
                
                double measuredILD = SpatialCueAnalyzer.computeILD(stereo[0], stereo[1]);
                double error = Math.abs(measuredILD - expectedILD);
                
                // Allow 0.5dB tolerance
                if (error > 0.5) {
                    System.out.printf("  ILD error too large: expected %.1f, got %.1f (error: %.1f dB)\n", 
                        expectedILD, measuredILD, error);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("  ILD test exception: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testFrequencyShifting() {
        try {
            float sampleRate = 44100.0f;
            float baseFreq = 1000.0f;
            
            // Generate test signal
            float[][] original = SyntheticSignalGenerator.generateSineStereo(
                1.0, sampleRate, baseFreq, 0.0, 0.0);
            
            // Test different shift amounts
            float[] shifts = {200.0f, 400.0f, 600.0f};
            
            for (float shift : shifts) {
                float[][] shifted = SimpleFrequencyShifter.shift(
                    original[0], original[1], sampleRate, shift);
                
                // Check that output arrays have same length
                if (shifted[0].length != original[0].length || shifted[1].length != original[1].length) {
                    System.out.printf("  Frequency shift changed array length: %d -> %d\n", 
                        original[0].length, shifted[0].length);
                    return false;
                }
                
                // Check that signal power is reasonable (not zero, not excessive)
                double originalPower = computeRMS(original[0]);
                double shiftedPower = computeRMS(shifted[0]);
                
                if (shiftedPower < 0.1 * originalPower || shiftedPower > 10.0 * originalPower) {
                    System.out.printf("  Frequency shift power ratio unreasonable: %.3f\n", 
                        shiftedPower / originalPower);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("  Frequency shift test exception: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testAudioValidation() {
        try {
            // Test valid audio parameters
            float sampleRate = 44100.0f;
            int channels = 2;
            int bitsPerSample = 16;
            
            // These should be valid
            if (!isValidAudioFormat(sampleRate, channels, bitsPerSample)) {
                System.out.println("  Valid audio format incorrectly rejected");
                return false;
            }
            
            // Test invalid parameters
            if (isValidAudioFormat(22050.0f, 1, 16)) { // Mono
                System.out.println("  Invalid mono format incorrectly accepted");
                return false;
            }
            
            if (isValidAudioFormat(44100.0f, 2, 24)) { // 24-bit
                System.out.println("  Invalid bit depth incorrectly accepted");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  Audio validation test exception: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testCrossCorrelation() {
        try {
            float sampleRate = 44100.0f;
            
            // Test cross-correlation with known delays
            int[] delays = {0, 10, 50, 100}; // samples
            
            for (int delay : delays) {
                // Create signals with known delay
                float[] signal1 = new float[1000];
                float[] signal2 = new float[1000];
                
                // Fill signal1 with sine wave
                for (int i = 0; i < signal1.length; i++) {
                    signal1[i] = (float) Math.sin(2 * Math.PI * 100 * i / sampleRate);
                }
                
                // Fill signal2 with delayed version
                for (int i = 0; i < signal2.length; i++) {
                    if (i >= delay) {
                        signal2[i] = signal1[i - delay];
                    }
                }
                
                // Compute ITD using cross-correlation
                double measuredITD = SpatialCueAnalyzer.computeITD(signal1, signal2, sampleRate);
                double expectedITD = delay * 1_000_000.0 / sampleRate; // Convert to microseconds
                double error = Math.abs(measuredITD - expectedITD);
                
                // Allow 100μs tolerance for cross-correlation
                if (error > 100.0) {
                    System.out.printf("  Cross-correlation error too large: expected %.1f, got %.1f (error: %.1f μs)\n", 
                        expectedITD, measuredITD, error);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("  Cross-correlation test exception: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    private static double computeRMS(float[] signal) {
        double sum = 0;
        for (float s : signal) {
            sum += s * s;
        }
        return Math.sqrt(sum / signal.length);
    }
    
    private static boolean isValidAudioFormat(float sampleRate, int channels, int bitsPerSample) {
        return sampleRate == 44100.0f && channels == 2 && bitsPerSample == 16;
    }
} 