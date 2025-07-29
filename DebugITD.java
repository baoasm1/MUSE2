import muse2.*;

public class DebugITD {
    public static void main(String[] args) {
        System.out.println("=== Debug ITD Measurement ===");
        
        float sampleRate = 44100.0f;
        float freq = 1000.0f;
        
        // Test with a simple delay
        System.out.println("Testing with 500μs ITD...");
        float[][] stereo = SyntheticSignalGenerator.generateSineStereo(1.0, sampleRate, freq, 500.0, 0.0);
        
        System.out.println("Signal generated. Left length: " + stereo[0].length + ", Right length: " + stereo[1].length);
        
        // Check if signals are different
        boolean signalsDifferent = false;
        for (int i = 0; i < Math.min(stereo[0].length, stereo[1].length); i++) {
            if (Math.abs(stereo[0][i] - stereo[1][i]) > 0.001) {
                signalsDifferent = true;
                break;
            }
        }
        System.out.println("Signals are different: " + signalsDifferent);
        
        // Measure ITD
        double measuredITD = SpatialCueAnalyzer.computeITD(stereo[0], stereo[1], sampleRate);
        System.out.println("Measured ITD: " + measuredITD + " μs");
        
        // Test cross-correlation directly
        System.out.println("\nTesting cross-correlation...");
        int maxLag = (int)(sampleRate * 0.001); // +/-1ms
        System.out.println("Max lag: " + maxLag + " samples");
        
        double maxCorr = Double.NEGATIVE_INFINITY;
        int bestLag = 0;
        
        for (int lag = -maxLag; lag <= maxLag; lag++) {
            double corr = crossCorrelation(stereo[0], stereo[1], lag);
            if (corr > maxCorr) {
                maxCorr = corr;
                bestLag = lag;
            }
        }
        
        System.out.println("Best lag: " + bestLag + " samples");
        System.out.println("Max correlation: " + maxCorr);
        System.out.println("Expected lag: " + (500.0 * sampleRate / 1_000_000.0) + " samples");
    }
    
    private static double crossCorrelation(float[] x, float[] y, int lag) {
        int len = Math.min(x.length, y.length);
        double sum = 0;
        for (int i = 0; i < len; i++) {
            int j = i + lag;
            if (j >= 0 && j < len) {
                sum += x[i] * y[j];
            }
        }
        return sum;
    }
} 