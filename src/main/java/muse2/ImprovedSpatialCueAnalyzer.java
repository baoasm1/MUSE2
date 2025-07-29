package muse2;

public class ImprovedSpatialCueAnalyzer {
    
    // Enhanced ITD measurement with improved accuracy
    public static double computeITD(float[] left, float[] right, float sampleRate) {
        // Use multiple methods and combine results for better accuracy
        double itd1 = crossCorrelationITD(left, right, sampleRate);
        double itd2 = phaseBasedITD(left, right, sampleRate);
        
        // Weighted combination (cross-correlation more reliable for larger ITDs)
        if (Math.abs(itd1) > 200) {
            return itd1; // Use cross-correlation for large ITDs
        } else {
            return (itd1 * 0.3 + itd2 * 0.7); // Weight phase-based more for small ITDs
        }
    }
    
    // Improved cross-correlation with sub-sample interpolation
    private static double crossCorrelationITD(float[] left, float[] right, float sampleRate) {
        int maxLag = (int)(sampleRate * 0.001); // +/-1ms window
        double bestLag = 0;
        double maxCorr = Double.NEGATIVE_INFINITY;
        
        // Fine-grained search with interpolation
        for (double lag = -maxLag; lag <= maxLag; lag += 0.1) {
            double corr = interpolatedCrossCorrelation(left, right, lag);
            if (corr > maxCorr) {
                maxCorr = corr;
                bestLag = lag;
            }
        }
        
        // Convert lag (samples) to microseconds
        return bestLag * 1_000_000.0 / sampleRate;
    }
    
    // Interpolated cross-correlation for sub-sample accuracy
    private static double interpolatedCrossCorrelation(float[] x, float[] y, double lag) {
        int len = Math.min(x.length, y.length);
        double sum = 0;
        int count = 0;
        
        if (lag >= 0) {
            for (int i = 0; i < len - Math.ceil(lag); i++) {
                double rightIndex = i + lag;
                int rightIndexInt = (int)rightIndex;
                double fraction = rightIndex - rightIndexInt;
                
                if (rightIndexInt + 1 < y.length) {
                    // Linear interpolation
                    double yValue = y[rightIndexInt] * (1 - fraction) + y[rightIndexInt + 1] * fraction;
                    sum += x[i] * yValue;
                    count++;
                }
            }
        } else {
            for (int i = (int)Math.ceil(-lag); i < len; i++) {
                double leftIndex = i + lag;
                int leftIndexInt = (int)leftIndex;
                double fraction = leftIndex - leftIndexInt;
                
                if (leftIndexInt + 1 < x.length) {
                    // Linear interpolation
                    double xValue = x[leftIndexInt] * (1 - fraction) + x[leftIndexInt + 1] * fraction;
                    sum += xValue * y[i];
                    count++;
                }
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    // Phase-based ITD estimation (more accurate for small ITDs)
    private static double phaseBasedITD(float[] left, float[] right, float sampleRate) {
        // Use FFT to get phase difference at dominant frequency
        int fftSize = 1024;
        double[] leftFFT = fft(left, fftSize);
        double[] rightFFT = fft(right, fftSize);
        
        // Find dominant frequency
        int dominantBin = findDominantFrequency(leftFFT);
        if (dominantBin == 0) return 0;
        
        // Calculate phase difference
        double leftPhase = Math.atan2(leftFFT[dominantBin * 2 + 1], leftFFT[dominantBin * 2]);
        double rightPhase = Math.atan2(rightFFT[dominantBin * 2 + 1], rightFFT[dominantBin * 2]);
        
        double phaseDiff = rightPhase - leftPhase;
        
        // Normalize phase difference
        while (phaseDiff > Math.PI) phaseDiff -= 2 * Math.PI;
        while (phaseDiff < -Math.PI) phaseDiff += 2 * Math.PI;
        
        // Convert to ITD
        double frequency = dominantBin * sampleRate / fftSize;
        return phaseDiff / (2 * Math.PI * frequency) * 1_000_000; // Convert to microseconds
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
    
    // Enhanced ILD measurement with frequency weighting
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
    
    // Validation method to test accuracy
    public static double validateITDMeasurement(float[] left, float[] right, float sampleRate, double expectedITD) {
        double measuredITD = computeITD(left, right, sampleRate);
        return Math.abs(measuredITD - expectedITD);
    }
} 