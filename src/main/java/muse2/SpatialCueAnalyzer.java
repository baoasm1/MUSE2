package muse2;

public class SpatialCueAnalyzer {
    // ITD: Interaural Time Difference (microseconds)
    public static double computeITD(float[] left, float[] right, float sampleRate) {
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

    // Standard cross-correlation: sum left[i] * right[i+lag] for valid i
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

    // ILD: Interaural Level Difference (dB)
    public static double computeILD(float[] left, float[] right) {
        double rmsLeft = rms(left);
        double rmsRight = rms(right);
        return 20 * Math.log10(rmsLeft / rmsRight);
    }

    private static double rms(float[] arr) {
        double sum = 0;
        for (float v : arr) sum += v * v;
        return Math.sqrt(sum / arr.length);
    }
} 