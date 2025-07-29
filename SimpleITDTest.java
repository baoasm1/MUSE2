package muse2;

import javax.sound.sampled.*;
import java.io.*;

public class SimpleITDTest {
    private static final int SAMPLE_RATE = 44100;
    
    public static void main(String[] args) {
        try {
            // Create test signals with known ITDs
            double[] knownITDs = {-500, -250, -100, 0, 100, 250, 500};
            
            System.out.println("=== ITD VERIFICATION TEST ===");
            System.out.println("Creating and analyzing test signals with known ITDs...\n");
            
            for (double knownITD : knownITDs) {
                // Generate test signal
                String filename = "itd_test_" + (int)knownITD + "us.wav";
                createTestSignal(knownITD, filename);
                
                // Analyze it
                muse2.SpatialCueAnalyzer.SpatialCues cues = muse2.SpatialCueAnalyzer.analyzeStereoFile(filename);
                double error = Math.abs(cues.getItdMicroseconds() - knownITD);
                
                System.out.printf("Expected: %7.2fμs, Measured: %7.2fμs, Error: %7.2fμs%n",
                                 knownITD, cues.getItdMicroseconds(), error);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a test signal with a specific ITD
     * This method creates a clean sine wave with a precise ITD
     */
    public static void createTestSignal(double itdMicroseconds, String filename) 
            throws IOException {
        // Convert ITD to samples
        double itdSamples = itdMicroseconds * SAMPLE_RATE / 1000000.0;
        int delay = (int)Math.round(itdSamples);
        
        // Create signals
        int durationSamples = SAMPLE_RATE; // 1 second
        float[] leftChannel = new float[durationSamples];
        float[] rightChannel = new float[durationSamples];
        
        // Generate 1kHz sine wave
        double frequency = 1000.0;
        for (int i = 0; i < durationSamples; i++) {
            double phase = 2 * Math.PI * frequency * i / SAMPLE_RATE;
            leftChannel[i] = (float)Math.sin(phase);
        }
        
        // Apply delay to right channel
        for (int i = 0; i < durationSamples; i++) {
            int j = i - delay;
            if (j >= 0 && j < durationSamples) {
                rightChannel[i] = leftChannel[j];
            }
        }
        
        // Combine channels into stereo file
        saveToWavFile(leftChannel, rightChannel, filename);
    }
    
    private static void saveToWavFile(float[] leftChannel, float[] rightChannel, String filename)
            throws IOException {
        // Create audio format
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
        
        // Convert float samples to bytes
        byte[] audioBytes = new byte[leftChannel.length * 4]; // 2 bytes per sample, 2 channels
        
        for (int i = 0; i < leftChannel.length; i++) {
            // Left channel
            short leftSample = (short)(leftChannel[i] * 32767.0f);
            audioBytes[i*4] = (byte)(leftSample & 0xFF);
            audioBytes[i*4+1] = (byte)(leftSample >> 8);
            
            // Right channel
            short rightSample = (short)(rightChannel[i] * 32767.0f);
            audioBytes[i*4+2] = (byte)(rightSample & 0xFF);
            audioBytes[i*4+3] = (byte)(rightSample >> 8);
        }
        
        // Save to WAV file
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream ais = new AudioInputStream(bais, format, leftChannel.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
    }
} 