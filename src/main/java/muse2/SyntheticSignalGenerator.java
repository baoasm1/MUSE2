package muse2;

public class SyntheticSignalGenerator {
    // Generate a stereo sine wave with specified ITD (us) and ILD (dB)
    public static float[][] generateSineStereo(double durationSec, float sampleRate, float freqHz, double itdUs, double ildDb) {
        int nSamples = (int)(durationSec * sampleRate);
        float[] left = new float[nSamples];
        float[] right = new float[nSamples];
        int itdSamples = (int)Math.round(itdUs * sampleRate / 1_000_000.0);
        double ildLinear = Math.pow(10, ildDb / 20.0);
        // Generate the base sine wave
        for (int i = 0; i < nSamples; i++) {
            double t = i / sampleRate;
            left[i] = (float)Math.sin(2 * Math.PI * freqHz * t);
        }
        // Apply ITD and ILD: right channel is delayed version of left
        if (itdSamples >= 0) {
            // Right ear delayed (positive ITD)
            for (int i = 0; i < nSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed (negative ITD)
            for (int i = 0; i < nSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        }
        return new float[][] { left, right };
    }
} 