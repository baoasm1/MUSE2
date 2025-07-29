package muse2;

public class SimpleFrequencyShifter {
    // Simple frequency shifting using basic signal processing
    public static float[][] shift(float[] left, float[] right, float sampleRate, float shiftAmountHz) {
        // For demonstration, we'll apply a simple phase shift
        // This is not as sophisticated as TarsosDSP but will work for testing
        
        float[][] shifted = new float[2][];
        shifted[0] = new float[left.length];
        shifted[1] = new float[right.length];
        
        // Simple frequency shift simulation
        float phaseShift = (float) (2 * Math.PI * shiftAmountHz / sampleRate);
        
        for (int i = 0; i < left.length; i++) {
            // Apply a simple phase shift to simulate frequency lowering
            shifted[0][i] = (float) (left[i] * Math.cos(phaseShift * i));
            if (i < right.length) {
                shifted[1][i] = (float) (right[i] * Math.cos(phaseShift * i));
            }
        }
        
        return shifted;
    }
} 