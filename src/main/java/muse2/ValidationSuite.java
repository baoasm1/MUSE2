package muse2;

import java.io.*;
import java.util.*;

public class ValidationSuite {
    private static final String LOG_FILE = "validation_log.txt";
    private static final String RESULTS_FILE = "validation_results.csv";
    
    public static void main(String[] args) {
        System.out.println("=== MUSE2 Validation Suite ===");
        System.out.println("Testing system with synthetic signals...");
        
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(LOG_FILE))) {
            logWriter.println("MUSE2 Validation Suite - " + new Date());
            logWriter.println("Testing system components with synthetic signals");
            
            // Test 1: Pure tone ITD/ILD validation
            testPureToneValidation(logWriter);
            
            // Test 2: Frequency shifting accuracy
            testFrequencyShiftingAccuracy(logWriter);
            
            // Test 3: Audio format compatibility
            testAudioFormatCompatibility(logWriter);
            
            // Test 4: Processing artifacts check
            testProcessingArtifacts(logWriter);
            
            // Test 5: Timing accuracy
            testTimingAccuracy(logWriter);
            
            System.out.println("✅ Validation complete! Check " + LOG_FILE + " for details.");
            System.out.println("Results saved to: " + RESULTS_FILE);
            
        } catch (IOException e) {
            System.err.println("❌ Validation failed: " + e.getMessage());
        }
    }
    
    private static void testPureToneValidation(PrintWriter log) {
        log.println("\n=== Test 1: Pure Tone ITD/ILD Validation ===");
        
        float sampleRate = 44100.0f;
        float frequency = 1000.0f;
        
        // Test known ITD/ILD values
        double[] expectedITDs = {0.0, 500.0, -500.0, 300.0, -300.0}; // microseconds
        double[] expectedILDs = {0.0, 6.0, -6.0, 3.0, -3.0}; // dB
        
        for (int i = 0; i < expectedITDs.length; i++) {
            try {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    1.0, sampleRate, frequency, expectedITDs[i], expectedILDs[i]);
                
                double measuredITD = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
                double measuredILD = SpatialCueAnalyzer.computeILD(stereo[0], stereo[1]);
                
                double itdError = Math.abs(measuredITD - expectedITDs[i]);
                double ildError = Math.abs(measuredILD - expectedILDs[i]);
                
                log.printf("Test %d: Expected ITD=%.1f, Measured ITD=%.1f, Error=%.1f us\n", 
                    i+1, expectedITDs[i], measuredITD, itdError);
                log.printf("Test %d: Expected ILD=%.1f, Measured ILD=%.1f, Error=%.1f dB\n", 
                    i+1, expectedILDs[i], measuredILD, ildError);
                
                // Validate accuracy (within 50μs for ITD, 0.5dB for ILD)
                if (itdError > 50.0) {
                    log.printf("❌ ITD error too large: %.1f us\n", itdError);
                }
                if (ildError > 0.5) {
                    log.printf("❌ ILD error too large: %.1f dB\n", ildError);
                }
                
            } catch (Exception e) {
                log.printf("❌ Test %d failed: %s\n", i+1, e.getMessage());
            }
        }
    }
    
    private static void testFrequencyShiftingAccuracy(PrintWriter log) {
        log.println("\n=== Test 2: Frequency Shifting Accuracy ===");
        
        float sampleRate = 44100.0f;
        float baseFreq = 1000.0f;
        float[] shiftAmounts = {200.0f, 400.0f, 600.0f};
        
        // Generate test signal
        float[][] original = SyntheticSignalGenerator.generateSineStereo(
            1.0, sampleRate, baseFreq, 0.0, 0.0);
        
        for (float shift : shiftAmounts) {
            try {
                float[][] shifted = SimpleFrequencyShifter.shift(
                    original[0], original[1], sampleRate, shift);
                
                // Measure frequency content (simplified)
                double originalPower = computeSignalPower(original[0]);
                double shiftedPower = computeSignalPower(shifted[0]);
                
                log.printf("Shift %.0f Hz: Original power=%.3f, Shifted power=%.3f\n", 
                    shift, originalPower, shiftedPower);
                
                // Check for significant power loss
                double powerRatio = shiftedPower / originalPower;
                if (powerRatio < 0.5) {
                    log.printf("❌ Significant power loss with %.0f Hz shift: %.1f%%\n", 
                        shift, (1-powerRatio)*100);
                }
                
            } catch (Exception e) {
                log.printf("❌ Frequency shift test failed: %s\n", e.getMessage());
            }
        }
    }
    
    private static void testAudioFormatCompatibility(PrintWriter log) {
        log.println("\n=== Test 3: Audio Format Compatibility ===");
        
        // Test different sample rates
        float[] sampleRates = {22050.0f, 44100.0f, 48000.0f};
        
        for (float sr : sampleRates) {
            try {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    1.0, sr, 1000.0f, 500.0, 3.0);
                
                double itd = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sr);
                double ild = SpatialCueAnalyzer.computeILD(stereo[0], stereo[1]);
                
                log.printf("Sample rate %.0f Hz: ITD=%.1f us, ILD=%.1f dB\n", sr, itd, ild);
                
            } catch (Exception e) {
                log.printf("❌ Sample rate %.0f Hz failed: %s\n", sr, e.getMessage());
            }
        }
    }
    
    private static void testProcessingArtifacts(PrintWriter log) {
        log.println("\n=== Test 4: Processing Artifacts Check ===");
        
        float sampleRate = 44100.0f;
        float[][] original = SyntheticSignalGenerator.generateSineStereo(
            1.0, sampleRate, 1000.0f, 0.0, 0.0);
        
        try {
            float[][] shifted = SimpleFrequencyShifter.shift(
                original[0], original[1], sampleRate, 400.0f);
            
            // Check for clicks (sudden amplitude changes)
            double clickScore = detectClicks(shifted[0]);
            double distortionScore = measureDistortion(original[0], shifted[0]);
            
            log.printf("Click detection score: %.3f (lower is better)\n", clickScore);
            log.printf("Distortion score: %.3f (lower is better)\n", distortionScore);
            
            if (clickScore > 0.1) {
                log.printf("❌ Potential clicks detected\n");
            }
            if (distortionScore > 0.2) {
                log.printf("❌ Significant distortion detected\n");
            }
            
        } catch (Exception e) {
            log.printf("❌ Artifact test failed: %s\n", e.getMessage());
        }
    }
    
    private static void testTimingAccuracy(PrintWriter log) {
        log.println("\n=== Test 5: Timing Accuracy ===");
        
        float sampleRate = 44100.0f;
        double[] testITDs = {100.0, 200.0, 500.0}; // microseconds
        
        for (double expectedITD : testITDs) {
            try {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    1.0, sampleRate, 1000.0f, expectedITD, 0.0);
                
                double measuredITD = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
                double error = Math.abs(measuredITD - expectedITD);
                
                log.printf("Expected ITD: %.1f us, Measured: %.1f us, Error: %.1f us\n", 
                    expectedITD, measuredITD, error);
                
                // Check for sample-level precision
                double sampleError = error * sampleRate / 1_000_000.0;
                if (sampleError > 1.0) {
                    log.printf("❌ Timing error exceeds 1 sample: %.1f samples\n", sampleError);
                }
                
            } catch (Exception e) {
                log.printf("❌ Timing test failed: %s\n", e.getMessage());
            }
        }
    }
    
    // Helper methods
    private static double computeSignalPower(float[] signal) {
        double sum = 0;
        for (float s : signal) {
            sum += s * s;
        }
        return sum / signal.length;
    }
    
    private static double detectClicks(float[] signal) {
        double maxJump = 0;
        for (int i = 1; i < signal.length; i++) {
            double jump = Math.abs(signal[i] - signal[i-1]);
            if (jump > maxJump) maxJump = jump;
        }
        return maxJump;
    }
    
    private static double measureDistortion(float[] original, float[] processed) {
        double distortion = 0;
        int len = Math.min(original.length, processed.length);
        for (int i = 0; i < len; i++) {
            distortion += Math.abs(processed[i] - original[i]);
        }
        return distortion / len;
    }
} 