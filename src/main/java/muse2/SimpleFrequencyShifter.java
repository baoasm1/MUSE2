package muse2;

public class SimpleFrequencyShifter {
    
    /**
     * Simple frequency shifter using phase shift method
     * @param left Left channel audio data
     * @param right Right channel audio data
     * @param sampleRate Sample rate in Hz
     * @param shiftAmountHz Amount to shift frequency (positive = upward, negative = downward)
     * @return Shifted stereo audio data [left, right]
     */
    public static float[][] shift(float[] left, float[] right, float sampleRate, float shiftAmountHz) {
        float[][] shifted = new float[2][];
        shifted[0] = new float[left.length];
        shifted[1] = new float[right.length];
        
        // Calculate phase shift per sample
        float phaseShift = (float) (2 * Math.PI * shiftAmountHz / sampleRate);
        
        // Apply frequency shift to both channels
        for (int i = 0; i < left.length; i++) {
            // Simple phase shift method
            shifted[0][i] = (float) (left[i] * Math.cos(phaseShift * i));
            
            if (i < right.length) {
                shifted[1][i] = (float) (right[i] * Math.cos(phaseShift * i));
            }
        }
        
        return shifted;
    }
    
    /**
     * Advanced frequency shifter with improved quality
     * @param left Left channel audio data
     * @param right Right channel audio data
     * @param sampleRate Sample rate in Hz
     * @param shiftAmountHz Amount to shift frequency
     * @return Shifted stereo audio data [left, right]
     */
    public static float[][] shiftAdvanced(float[] left, float[] right, float sampleRate, float shiftAmountHz) {
        float[][] shifted = new float[2][];
        shifted[0] = new float[left.length];
        shifted[1] = new float[right.length];
        
        // Calculate shift ratio
        double shiftRatio = 1.0 + (shiftAmountHz / 1000.0); // Relative to 1kHz
        
        // Apply time-stretching and resampling
        for (int i = 0; i < left.length; i++) {
            // Calculate new sample position
            double newPos = i * shiftRatio;
            int pos1 = (int) newPos;
            int pos2 = pos1 + 1;
            
            // Linear interpolation
            if (pos1 < left.length - 1) {
                double weight = newPos - pos1;
                shifted[0][i] = (float) (left[pos1] * (1 - weight) + left[pos2] * weight);
                
                if (pos1 < right.length - 1) {
                    shifted[1][i] = (float) (right[pos1] * (1 - weight) + right[pos2] * weight);
                }
            } else {
                shifted[0][i] = left[Math.min(pos1, left.length - 1)];
                if (i < right.length) {
                    shifted[1][i] = right[Math.min(pos1, right.length - 1)];
                }
            }
        }
        
        return shifted;
    }
    
    /**
     * Check if frequency shift preserves signal quality
     * @param original Original signal power
     * @param shifted Shifted signal power
     * @return True if quality is preserved (power ratio >= 0.5)
     */
    public static boolean checkQualityPreservation(double original, double shifted) {
        return (shifted / original) >= 0.5;
    }
    
    /**
     * Calculate signal power for quality assessment
     * @param signal Audio signal
     * @return Signal power
     */
    public static double calculateSignalPower(float[] signal) {
        double sum = 0;
        for (float sample : signal) {
            sum += sample * sample;
        }
        return sum / signal.length;
    }
} 