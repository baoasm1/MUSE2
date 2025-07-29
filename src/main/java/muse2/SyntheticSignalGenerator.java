package muse2;

public class SyntheticSignalGenerator {
    
    /**
     * Generate stereo sine wave with specified ITD and ILD
     * @param durationSec Duration in seconds
     * @param sampleRate Sample rate in Hz
     * @param freqHz Frequency in Hz
     * @param itdUs ITD in microseconds (positive = right ear delayed)
     * @param ildDb ILD in decibels (positive = left ear louder)
     * @return Stereo audio data [left, right]
     */
    public static float[][] generateSineStereo(double durationSec, float sampleRate, float freqHz, double itdUs, double ildDb) {
        int nSamples = (int) (sampleRate * durationSec);
        float[] left = new float[nSamples];
        float[] right = new float[nSamples];
        
        // Convert ITD from microseconds to samples
        int itdSamples = (int) (itdUs * sampleRate / 1_000_000);
        
        // Convert ILD from dB to linear ratio
        double ildLinear = Math.pow(10, ildDb / 20);
        
        // Generate the base sine wave for left channel
        for (int i = 0; i < nSamples; i++) {
            double t = i / sampleRate;
            left[i] = (float)Math.sin(2 * Math.PI * freqHz * t);
        }
        
        // Apply ITD and ILD to right channel
        if (itdSamples >= 0) {
            // Right ear delayed (positive ITD)
            for (int i = 0; i < nSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0 && j < nSamples) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed (negative ITD)
            for (int i = 0; i < nSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0 && j < nSamples) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
    
    /**
     * Generate speech-like signal with multiple frequencies
     * @param durationSec Duration in seconds
     * @param sampleRate Sample rate in Hz
     * @param itdUs ITD in microseconds
     * @param ildDb ILD in decibels
     * @return Stereo audio data [left, right]
     */
    public static float[][] generateSpeechLikeSignal(double durationSec, float sampleRate, double itdUs, double ildDb) {
        int nSamples = (int) (sampleRate * durationSec);
        float[] left = new float[nSamples];
        float[] right = new float[nSamples];
        
        // Convert ITD from microseconds to samples
        int itdSamples = (int) (itdUs * sampleRate / 1_000_000);
        
        // Convert ILD from dB to linear ratio
        double ildLinear = Math.pow(10, ildDb / 20);
        
        // Generate speech-like signal with multiple frequencies
        float[] frequencies = {500, 1000, 2000, 4000}; // Speech frequency range
        float[] amplitudes = {0.3f, 0.5f, 0.4f, 0.2f}; // Relative amplitudes
        
        for (int i = 0; i < nSamples; i++) {
            double t = i / sampleRate;
            double sample = 0;
            
            // Sum multiple frequencies
            for (int f = 0; f < frequencies.length; f++) {
                sample += amplitudes[f] * Math.sin(2 * Math.PI * frequencies[f] * t);
            }
            
            left[i] = (float)sample;
        }
        
        // Apply ITD and ILD
        if (itdSamples >= 0) {
            // Right ear delayed
            for (int i = 0; i < nSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0 && j < nSamples) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed
            for (int i = 0; i < nSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0 && j < nSamples) {
                    right[i] = (float)(left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
    
    /**
     * Generate signal with controlled noise
     * @param durationSec Duration in seconds
     * @param sampleRate Sample rate in Hz
     * @param freqHz Frequency in Hz
     * @param itdUs ITD in microseconds
     * @param ildDb ILD in decibels
     * @param noiseLevel Noise level (0.0 to 1.0)
     * @return Stereo audio data [left, right]
     */
    public static float[][] generateNoisySignal(double durationSec, float sampleRate, float freqHz, double itdUs, double ildDb, double noiseLevel) {
        int nSamples = (int) (sampleRate * durationSec);
        float[] left = new float[nSamples];
        float[] right = new float[nSamples];
        
        // Convert ITD from microseconds to samples
        int itdSamples = (int) (itdUs * sampleRate / 1_000_000);
        
        // Convert ILD from dB to linear ratio
        double ildLinear = Math.pow(10, ildDb / 20);
        
        // Generate base signal with noise
        for (int i = 0; i < nSamples; i++) {
            double t = i / sampleRate;
            double signal = Math.sin(2 * Math.PI * freqHz * t);
            double noise = (Math.random() - 0.5) * 2 * noiseLevel;
            left[i] = (float)(signal + noise);
        }
        
        // Apply ITD and ILD with noise
        if (itdSamples >= 0) {
            // Right ear delayed
            for (int i = 0; i < nSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0 && j < nSamples) {
                    double signal = left[j];
                    double noise = (Math.random() - 0.5) * 2 * noiseLevel;
                    right[i] = (float)((signal + noise) / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed
            for (int i = 0; i < nSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0 && j < nSamples) {
                    double signal = left[j];
                    double noise = (Math.random() - 0.5) * 2 * noiseLevel;
                    right[i] = (float)((signal + noise) / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
} 