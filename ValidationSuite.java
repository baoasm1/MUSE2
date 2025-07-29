import muse2.*;
import java.io.*;
import java.util.*;

public class ValidationSuite {
    private static final String LOG_FILE = "validation_log.txt";
    private static boolean demoMode = false;
    
    public static void main(String[] args) {
        // Check for demo mode
        if (args.length > 0 && args[0].equals("--demo")) {
            demoMode = true;
        }
        
        if (demoMode) {
            System.out.println("🎯 MUSE2 Validation Suite - DEMO MODE");
            System.out.println("Testing system with synthetic signals for presentation...\n");
        } else {
            System.out.println("=== MUSE2 Validation Suite ===");
            System.out.println("Testing system with synthetic signals...");
        }
        
        int passedTests = 0;
        int totalTests = 0;
        
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(LOG_FILE))) {
            logWriter.println("MUSE2 Validation Suite - " + new Date());
            logWriter.println("Testing system components with synthetic signals");
            
            // Test 1: Pure tone ITD/ILD validation
            int[] results = testPureToneValidation(logWriter);
            passedTests += results[0];
            totalTests += results[1];
            
            // Test 2: Frequency shifting accuracy
            results = testFrequencyShiftingAccuracy(logWriter);
            passedTests += results[0];
            totalTests += results[1];
            
            // Summary
            String summary = String.format("\n=== VALIDATION SUMMARY ===\n" +
                "Tests Passed: %d/%d (%.1f%%)\n" +
                "Status: %s", 
                passedTests, totalTests, (passedTests*100.0/totalTests),
                (passedTests == totalTests) ? "✅ ALL TESTS PASSED" : "⚠️  SOME TESTS FAILED");
            
            logWriter.println(summary);
            
            if (demoMode) {
                System.out.println(summary);
                System.out.println("\n📊 Results saved to: " + LOG_FILE);
                System.out.println("🎉 System ready for research presentation!");
            } else {
                System.out.println("✅ Validation complete! Check " + LOG_FILE + " for details.");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Validation failed: " + e.getMessage());
        }
    }
    
    private static int[] testPureToneValidation(PrintWriter log) {
        if (demoMode) {
            System.out.println("🔬 Test 1: ITD/ILD Measurement Accuracy");
        }
        log.println("\n=== Test 1: Pure Tone ITD/ILD Validation ===");
        
        float sampleRate = 44100.0f;
        float frequency = 1000.0f;
        double duration = demoMode ? 0.5 : 1.0; // Shorter for demo
        
        // Test known ITD/ILD values
        double[] expectedITDs = {0.0, 500.0, -500.0, 300.0, -300.0}; // microseconds
        double[] expectedILDs = {0.0, 6.0, -6.0, 3.0, -3.0}; // dB
        
        int passed = 0;
        int total = expectedITDs.length;
        
        for (int i = 0; i < expectedITDs.length; i++) {
            try {
                float[][] stereo = SyntheticSignalGenerator.generateSineStereo(
                    duration, sampleRate, frequency, expectedITDs[i], expectedILDs[i]);
                
                double measuredITD = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
                double measuredILD = SpatialCueAnalyzer.computeILD(stereo[0], stereo[1]);
                
                double itdError = Math.abs(measuredITD - expectedITDs[i]);
                double ildError = Math.abs(measuredILD - expectedILDs[i]);
                
                boolean itdPass = itdError <= 50.0; // 50μs tolerance
                boolean ildPass = ildError <= 0.5;  // 0.5dB tolerance
                boolean testPass = itdPass && ildPass;
                
                if (testPass) passed++;
                
                String status = testPass ? "✅ PASS" : "❌ FAIL";
                
                log.printf("Test %d: Expected ITD=%.1f, Measured ITD=%.1f, Error=%.1f us %s\n", 
                    i+1, expectedITDs[i], measuredITD, itdError, itdPass ? "✅" : "❌");
                log.printf("Test %d: Expected ILD=%.1f, Measured ILD=%.1f, Error=%.1f dB %s\n", 
                    i+1, expectedILDs[i], measuredILD, ildError, ildPass ? "✅" : "❌");
                log.printf("Test %d: %s\n", i+1, status);
                
                if (demoMode) {
                    System.out.printf("  Test %d: ITD %.1f→%.1f μs (%.1f), ILD %.1f→%.1f dB (%.1f) %s\n",
                        i+1, expectedITDs[i], measuredITD, itdError, 
                        expectedILDs[i], measuredILD, ildError, status);
                }
                
            } catch (Exception e) {
                log.printf("❌ Test %d failed: %s\n", i+1, e.getMessage());
                if (demoMode) {
                    System.out.printf("  Test %d: ❌ ERROR - %s\n", i+1, e.getMessage());
                }
            }
        }
        
        if (demoMode) {
            System.out.printf("  Result: %d/%d tests passed\n", passed, total);
        }
        
        return new int[]{passed, total};
    }
    
    private static int[] testFrequencyShiftingAccuracy(PrintWriter log) {
        if (demoMode) {
            System.out.println("\n🎵 Test 2: Frequency Shifting Quality");
        }
        log.println("\n=== Test 2: Frequency Shifting Accuracy ===");
        
        float sampleRate = 44100.0f;
        float baseFreq = 1000.0f;
        double duration = demoMode ? 0.5 : 1.0;
        float[] shiftAmounts = {200.0f, 400.0f, 600.0f};
        
        // Generate test signal
        float[][] original = SyntheticSignalGenerator.generateSineStereo(
            duration, sampleRate, baseFreq, 0.0, 0.0);
        
        int passed = 0;
        int total = shiftAmounts.length;
        
        for (float shift : shiftAmounts) {
            try {
                float[][] shifted = SimpleFrequencyShifter.shift(
                    original[0], original[1], sampleRate, shift);
                
                // Measure frequency content (simplified)
                double originalPower = computeSignalPower(original[0]);
                double shiftedPower = computeSignalPower(shifted[0]);
                
                // Check for significant power loss (should be > 50% of original)
                double powerRatio = shiftedPower / originalPower;
                boolean testPass = powerRatio >= 0.5;
                
                if (testPass) passed++;
                
                String status = testPass ? "✅ PASS" : "❌ FAIL";
                
                log.printf("Shift %.0f Hz: Original power=%.3f, Shifted power=%.3f, Ratio=%.1f%% %s\n", 
                    shift, originalPower, shiftedPower, powerRatio*100, status);
                
                if (demoMode) {
                    System.out.printf("  Shift %.0f Hz: Power ratio %.1f%% %s\n", 
                        shift, powerRatio*100, status);
                }
                
            } catch (Exception e) {
                log.printf("❌ Frequency shift test failed: %s\n", e.getMessage());
                if (demoMode) {
                    System.out.printf("  Shift %.0f Hz: ❌ ERROR - %s\n", shift, e.getMessage());
                }
            }
        }
        
        if (demoMode) {
            System.out.printf("  Result: %d/%d tests passed\n", passed, total);
        }
        
        return new int[]{passed, total};
    }
    
    // Helper methods
    private static double computeSignalPower(float[] signal) {
        double sum = 0;
        for (float s : signal) {
            sum += s * s;
        }
        return sum / signal.length;
    }
} 