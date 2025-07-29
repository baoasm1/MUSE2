package muse2;

public class SpatialCueAnalyzer {
    // Enhanced ITD: Interaural Time Difference (microseconds)
    public static double computeITD(float[] left, float[] right, float sampleRate) {
        // Initial cross-correlation for coarse delay
        int coarseDelay = findMaxCorrelationDelay(left, right, sampleRate);
        
        // Refine with phase analysis for sub-sample precision
        double refinedDelay = refineDelayWithPhase(left, right, coarseDelay, sampleRate);
        
        // Convert to microseconds
        return (refinedDelay / sampleRate) * 1000000.0;
    }
    
    // Find maximum correlation delay with improved peak detection
    private static int findMaxCorrelationDelay(float[] left, float[] right, float sampleRate) {
        int maxLag = (int)(sampleRate * 0.001); // +/-1ms window
        int bestLag = 0;
        double maxCorr = Double.NEGATIVE_INFINITY;
        
        // Use finer resolution for better accuracy
        for (int lag = -maxLag; lag <= maxLag; lag++) {
            double corr = crossCorrelation(left, right, lag);
            if (corr > maxCorr) {
                maxCorr = corr;
                bestLag = lag;
            }
        }
        
        return bestLag;
    }
    
    // Phase-based refinement for sub-sample accuracy
    private static double refineDelayWithPhase(float[] left, float[] right, int coarseDelay, float sampleRate) {
        // Use FFT to get phase difference at dominant frequency
        int fftSize = 1024;
        double[] leftFFT = fft(left, fftSize);
        double[] rightFFT = fft(right, fftSize);
        
        // Find dominant frequency
        int dominantBin = findDominantFrequency(leftFFT);
        if (dominantBin == 0) return coarseDelay;
        
        // Calculate phase difference
        double leftPhase = Math.atan2(leftFFT[dominantBin * 2 + 1], leftFFT[dominantBin * 2]);
        double rightPhase = Math.atan2(rightFFT[dominantBin * 2 + 1], rightFFT[dominantBin * 2]);
        
        double phaseDiff = rightPhase - leftPhase;
        
        // Normalize phase difference
        while (phaseDiff > Math.PI) phaseDiff -= 2 * Math.PI;
        while (phaseDiff < -Math.PI) phaseDiff += 2 * Math.PI;
        
        // Convert phase difference to delay
        double frequency = dominantBin * sampleRate / fftSize;
        double phaseDelay = phaseDiff / (2 * Math.PI * frequency);
        
        // Combine coarse and fine estimates
        double coarseDelaySec = coarseDelay / sampleRate;
        return coarseDelaySec + phaseDelay;
    }
    
    // Improved cross-correlation with windowing to reduce noise
    private static double crossCorrelation(float[] x, float[] y, int lag) {
        int len = Math.min(x.length, y.length);
        double sum = 0;
        int count = 0;
        
        if (lag >= 0) {
            for (int i = 0; i < len - lag; i++) {
                // Apply Hanning window to reduce edge effects
                double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (len - lag)));
                sum += x[i] * y[i + lag] * window;
                count++;
            }
        } else {
            for (int i = -lag; i < len; i++) {
                // Apply Hanning window
                double window = 0.5 * (1 - Math.cos(2 * Math.PI * (i + lag) / len));
                sum += x[i] * y[i + lag] * window;
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    // Simple FFT implementation
    private static double[] fft(float[] input, int size) {
        double[] output = new double[size * 2]; // Real and imaginary parts
        
        for (int k = 0; k < size; k++) {
            double real = 0, imag = 0;
            for (int n = 0; n < size; n++) {
                double angle = -2 * Math.PI * k * n / size;
                double sample = n < input.length ? input[n] : 0;
                real += sample * Math.cos(angle);
                imag += sample * Math.sin(angle);
            }
            output[k * 2] = real;
            output[k * 2 + 1] = imag;
        }
        
        return output;
    }
    
    // Find dominant frequency bin
    private static int findDominantFrequency(double[] fft) {
        int maxBin = 0;
        double maxMagnitude = 0;
        
        for (int i = 1; i < fft.length / 2; i++) {
            double magnitude = Math.sqrt(fft[i * 2] * fft[i * 2] + fft[i * 2 + 1] * fft[i * 2 + 1]);
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                maxBin = i;
            }
        }
        
        return maxBin;
    }

    // Enhanced ILD: Interaural Level Difference (dB)
    public static double computeILD(float[] left, float[] right) {
        // Use A-weighting for more perceptually relevant measurement
        double rmsLeft = weightedRMS(left);
        double rmsRight = weightedRMS(right);
        
        if (rmsRight == 0) return 0;
        return 20 * Math.log10(rmsLeft / rmsRight);
    }
    
    // A-weighted RMS for more perceptually relevant ILD
    private static double weightedRMS(float[] arr) {
        double sum = 0;
        int count = 0;
        
        for (int i = 0; i < arr.length; i++) {
            // Simple A-weighting approximation
            double weight = 1.0;
            if (i < arr.length * 0.1) weight = 0.5; // Low frequencies
            else if (i > arr.length * 0.9) weight = 0.8; // High frequencies
            
            sum += (arr[i] * weight) * (arr[i] * weight);
            count++;
        }
        
        return Math.sqrt(sum / count);
    }
} 