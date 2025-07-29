package muse2;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class SpatialCueAnalyzer {
    private static final Logger logger = Logger.getLogger(SpatialCueAnalyzer.class.getName());
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int MAX_LAG_SAMPLES = 100; // ~2.27ms at 44.1kHz
    
    public static class SpatialCues {
        private double itdMicroseconds;
        private double ildDecibels;
        
        public SpatialCues(double itdMicroseconds, double ildDecibels) {
            this.itdMicroseconds = itdMicroseconds;
            this.ildDecibels = ildDecibels;
        }
        
        public double getItdMicroseconds() { return itdMicroseconds; }
        public double getIldDecibels() { return ildDecibels; }
        
        @Override
        public String toString() {
            return String.format("ITD: %.2fμs, ILD: %.2fdB", itdMicroseconds, ildDecibels);
        }
    }
    
    /**
     * Analyzes ITD and ILD from stereo audio data
     * @param leftChannel Left channel audio data
     * @param rightChannel Right channel audio data
     * @param sampleRate Sample rate of the audio
     * @return SpatialCues containing ITD and ILD measurements
     */
    public static SpatialCues analyzeSpatialCues(float[] leftChannel, float[] rightChannel, int sampleRate) {
        // Verify input
        if (leftChannel == null || rightChannel == null || leftChannel.length == 0 || rightChannel.length == 0) {
            throw new IllegalArgumentException("Audio channels cannot be null or empty");
        }
        
        // Debug channel data (first few samples)
        logger.info("Left channel first 5 samples: " + Arrays.toString(Arrays.copyOf(leftChannel, 5)));
        logger.info("Right channel first 5 samples: " + Arrays.toString(Arrays.copyOf(rightChannel, 5)));
        
        // Calculate ITD using improved algorithm
        double itd = calculateImprovedITD(leftChannel, rightChannel, sampleRate);
        
        // Calculate ILD
        double ild = calculateILD(leftChannel, rightChannel);
        
        // Debug results
        logger.info(String.format("Analysis results: ITD = %.2fμs, ILD = %.2fdB", itd, ild));
        
        return new SpatialCues(itd, ild);
    }
    
    /**
     * Improved ITD calculation using normalized cross-correlation and parabolic interpolation
     * This avoids the 0.0 problem by ensuring proper normalization and interpolation
     */
    private static double calculateImprovedITD(float[] leftChannel, float[] rightChannel, int sampleRate) {
        // Ensure equal length by trimming to the shorter one
        int length = Math.min(leftChannel.length, rightChannel.length);
        if (length <= MAX_LAG_SAMPLES * 2) {
            logger.warning("Audio too short for accurate ITD measurement");
            return 0.0;
        }
        
        // Normalize signals to ensure proper correlation calculation
        float[] leftNorm = normalize(Arrays.copyOf(leftChannel, length));
        float[] rightNorm = normalize(Arrays.copyOf(rightChannel, length));
        
        double[] correlation = new double[MAX_LAG_SAMPLES * 2 + 1];
        int correlationCenter = MAX_LAG_SAMPLES;
        
        // Calculate cross-correlation
        for (int delay = -MAX_LAG_SAMPLES; delay <= MAX_LAG_SAMPLES; delay++) {
            double sum = 0.0;
            int count = 0;
            
            for (int i = 0; i < length; i++) {
                int j = i + delay;
                if (j >= 0 && j < length) {
                    sum += leftNorm[i] * rightNorm[j];
                    count++;
                }
            }
            
            // Store normalized correlation value
            correlation[correlationCenter + delay] = count > 0 ? sum / count : 0.0;
        }
        
        // Find peak correlation
        int maxIndex = 0;
        double maxCorrelation = correlation[0];
        
        for (int i = 1; i < correlation.length; i++) {
            if (correlation[i] > maxCorrelation) {
                maxCorrelation = correlation[i];
                maxIndex = i;
            }
        }
        
        // Handle edge cases
        if (maxIndex == 0 || maxIndex == correlation.length - 1) {
            logger.warning("Peak at correlation edge - may be inaccurate");
            int delay = maxIndex - correlationCenter;
            return (delay * 1000000.0) / sampleRate; // Convert to microseconds
        }
        
        // Parabolic interpolation for sub-sample accuracy
        double y1 = correlation[maxIndex - 1];
        double y2 = correlation[maxIndex];
        double y3 = correlation[maxIndex + 1];
        double d = (y3 - y1) / (2 * (2 * y2 - y1 - y3));
        
        // Refined delay with sub-sample precision
        double refinedDelay = (maxIndex - correlationCenter) + d;
        
        // Convert to microseconds
        double itdMicroseconds = (refinedDelay * 1000000.0) / sampleRate;
        
        logger.info("Calculated ITD: " + itdMicroseconds + "μs (peak at index " + maxIndex + 
                   ", interpolated offset " + d + ")");
        
        return itdMicroseconds;
    }
    
    private static double calculateILD(float[] leftChannel, float[] rightChannel) {
        double leftRMS = calculateRMS(leftChannel);
        double rightRMS = calculateRMS(rightChannel);
        
        // Prevent division by zero or very small values
        if (rightRMS < 0.000001) rightRMS = 0.000001;
        
        return 20 * Math.log10(leftRMS / rightRMS);
    }
    
    private static double calculateRMS(float[] signal) {
        double sum = 0;
        for (float sample : signal) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / signal.length);
    }
    
    private static float[] normalize(float[] signal) {
        // Find max absolute value
        float maxAbs = 0.0f;
        for (float sample : signal) {
            float abs = Math.abs(sample);
            if (abs > maxAbs) maxAbs = abs;
        }
        
        // Normalize
        float[] normalized = new float[signal.length];
        if (maxAbs > 0.000001f) {
            for (int i = 0; i < signal.length; i++) {
                normalized[i] = signal[i] / maxAbs;
            }
        }
        
        return normalized;
    }
    
    /**
     * Analyzes a stereo audio file and extracts spatial cues
     * @param audioFile Path to stereo audio file
     * @return SpatialCues containing ITD and ILD measurements
     */
    public static SpatialCues analyzeStereoFile(String audioFile) throws IOException, UnsupportedAudioFileException {
        logger.info("Analyzing spatial cues in: " + audioFile);
        
        // Load audio file
        File file = new File(audioFile);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioStream.getFormat();
        
        // Verify stereo
        if (format.getChannels() != 2) {
            throw new IllegalArgumentException("File must be stereo (2 channels): " + audioFile);
        }
        
        // Read audio data
        byte[] audioBytes = new byte[(int)audioStream.getFrameLength() * format.getFrameSize()];
        int bytesRead = audioStream.read(audioBytes);
        audioStream.close();
        
        if (bytesRead <= 0) {
            throw new IOException("Could not read audio data from: " + audioFile);
        }
        
        // Convert to float arrays (separate channels)
        float[][] channels = convertToFloatChannels(audioBytes, format);
        float[] leftChannel = channels[0];
        float[] rightChannel = channels[1];
        
        // Calculate ITD and ILD
        return analyzeSpatialCues(leftChannel, rightChannel, (int)format.getSampleRate());
    }
    
    private static float[][] convertToFloatChannels(byte[] audioBytes, AudioFormat format) {
        int numFrames = audioBytes.length / format.getFrameSize();
        float[][] channels = new float[2][numFrames];
        
        int sampleSizeInBytes = format.getSampleSizeInBits() / 8;
        boolean isBigEndian = format.isBigEndian();
        
        for (int i = 0; i < numFrames; i++) {
            int frameOffset = i * format.getFrameSize();
            
            // Extract left channel
            channels[0][i] = extractSample(audioBytes, frameOffset, sampleSizeInBytes, isBigEndian);
            
            // Extract right channel
            channels[1][i] = extractSample(audioBytes, frameOffset + sampleSizeInBytes, 
                                         sampleSizeInBytes, isBigEndian);
        }
        
        return channels;
    }
    
    private static float extractSample(byte[] audioBytes, int offset, int sampleSizeInBytes, boolean isBigEndian) {
        if (sampleSizeInBytes == 2) {
            // 16-bit sample
            short sample;
            if (isBigEndian) {
                sample = (short)((audioBytes[offset] << 8) | (audioBytes[offset + 1] & 0xFF));
            } else {
                sample = (short)((audioBytes[offset + 1] << 8) | (audioBytes[offset] & 0xFF));
            }
            return sample / 32768.0f;
        } else {
            // 8-bit sample
            return (audioBytes[offset] & 0xFF) / 128.0f - 1.0f;
        }
    }
    
    // Legacy methods for backward compatibility
    public static double computeITD(float[] left, float[] right, float sampleRate) {
        SpatialCues cues = analyzeSpatialCues(left, right, (int)sampleRate);
        return cues.getItdMicroseconds();
    }
    
    public static double computeILD(float[] left, float[] right) {
        return calculateILD(left, right);
    }
    
    /**
     * Simple test method for quick validation
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                // Analyze provided file
                SpatialCues cues = analyzeStereoFile(args[0]);
                System.out.println("Analysis results for " + args[0] + ":");
                System.out.println("ITD: " + cues.getItdMicroseconds() + "μs");
                System.out.println("ILD: " + cues.getIldDecibels() + "dB");
            } else {
                // Create and analyze test signal
                String testFile = "test_itd_signal.wav";
                System.out.println("Creating test signal with 250μs ITD...");
                
                // Use SyntheticSignalGenerator to create test signal if available
                try {
                    Class.forName("muse2.SyntheticSignalGenerator")
                         .getMethod("generateSignalWithKnownITD", double.class, double.class, String.class)
                         .invoke(null, 250.0, 1.0, testFile);
                } catch (Exception e) {
                    System.out.println("SyntheticSignalGenerator not available, can't create test signal");
                    return;
                }
                
                // Analyze generated test signal
                SpatialCues cues = analyzeStereoFile(testFile);
                System.out.println("Test signal analysis results:");
                System.out.println("Expected ITD: 250.00μs");
                System.out.println("Measured ITD: " + cues.getItdMicroseconds() + "μs");
                System.out.println("Error: " + Math.abs(cues.getItdMicroseconds() - 250.0) + "μs");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 