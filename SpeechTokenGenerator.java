import muse2.*;
import javax.sound.sampled.*;
import java.io.*;

public class SpeechTokenGenerator {
    public static void main(String[] args) {
        System.out.println("=== Speech Token Generator for Hearing Aid Research ===");
        System.out.println("Generating realistic speech-like tokens...");
        
        // Create input directory
        File inputDir = new File("input_wavs");
        if (!inputDir.exists()) {
            inputDir.mkdir();
        }
        
        float sampleRate = 44100.0f;
        
        // Generate speech-like tokens with different spatial characteristics
        generateSpeechToken("speech_front.wav", sampleRate, 0.0, 0.0, "front");
        generateSpeechToken("speech_left.wav", sampleRate, 500.0, 6.0, "left");
        generateSpeechToken("speech_right.wav", sampleRate, -500.0, -6.0, "right");
        generateSpeechToken("speech_45left.wav", sampleRate, 300.0, 3.0, "45deg_left");
        generateSpeechToken("speech_45right.wav", sampleRate, -300.0, -3.0, "45deg_right");
        
        // Generate different speech frequencies (representing different phonemes)
        generateSpeechToken("speech_low.wav", sampleRate, 0.0, 0.0, "low_freq", 500.0f);
        generateSpeechToken("speech_mid.wav", sampleRate, 0.0, 0.0, "mid_freq", 1500.0f);
        generateSpeechToken("speech_high.wav", sampleRate, 0.0, 0.0, "high_freq", 3000.0f);
        
        System.out.println("✅ Speech tokens generated in 'input_wavs' folder:");
        System.out.println("  - speech_front.wav (center, no spatial cues)");
        System.out.println("  - speech_left.wav (left side)");
        System.out.println("  - speech_right.wav (right side)");
        System.out.println("  - speech_45left.wav (45° left)");
        System.out.println("  - speech_45right.wav (45° right)");
        System.out.println("  - speech_low.wav (low frequency speech)");
        System.out.println("  - speech_mid.wav (mid frequency speech)");
        System.out.println("  - speech_high.wav (high frequency speech)");
        System.out.println("\nThese represent realistic speech scenarios for hearing aid testing.");
        System.out.println("\nNext: Run your analysis system on these tokens.");
    }
    
    private static void generateSpeechToken(String filename, float sampleRate, double itdUs, double ildDb, String description) {
        generateSpeechToken(filename, sampleRate, itdUs, ildDb, description, 1000.0f);
    }
    
    private static void generateSpeechToken(String filename, float sampleRate, double itdUs, double ildDb, String description, float baseFreq) {
        try {
            // Create more realistic speech-like signal
            float[][] stereo = generateSpeechLikeSignal(2.0, sampleRate, baseFreq, itdUs, ildDb);
            
            // Write to WAV file
            File outFile = new File("input_wavs", filename);
            writeStereoWav(stereo[0], stereo[1], sampleRate, outFile);
            
        } catch (Exception e) {
            System.err.println("Error generating " + filename + ": " + e.getMessage());
        }
    }
    
    private static float[][] generateSpeechLikeSignal(double durationSec, float sampleRate, float baseFreq, double itdUs, double ildDb) {
        int nSamples = (int)(durationSec * sampleRate);
        float[] left = new float[nSamples];
        float[] right = new float[nSamples];
        
        int itdSamples = (int)Math.round(itdUs * sampleRate / 1_000_000.0);
        double ildLinear = Math.pow(10, ildDb / 20.0);
        
        for (int i = 0; i < nSamples; i++) {
            double t = i / sampleRate;
            
            // Create speech-like signal with multiple harmonics and formants
            double fundamental = Math.sin(2 * Math.PI * baseFreq * t);
            double harmonic1 = 0.7 * Math.sin(2 * Math.PI * baseFreq * 2 * t);
            double harmonic2 = 0.5 * Math.sin(2 * Math.PI * baseFreq * 3 * t);
            double formant1 = 0.3 * Math.sin(2 * Math.PI * (baseFreq + 500) * t);
            double formant2 = 0.2 * Math.sin(2 * Math.PI * (baseFreq + 1500) * t);
            
            // Combine harmonics and formants
            double sample = fundamental + harmonic1 + harmonic2 + formant1 + formant2;
            
            // Apply envelope to simulate speech onset/offset
            double envelope = Math.exp(-Math.abs(t - durationSec/2) / (durationSec/4));
            sample *= envelope;
            
            // Add some noise to make it more realistic
            sample += 0.05 * (Math.random() - 0.5);
            
            left[i] = (float)sample;
            
            // Apply ITD (delay) and ILD (level difference)
            int rightIdx = i - itdSamples;
            if (rightIdx >= 0 && rightIdx < nSamples) {
                right[i] = (float)(sample / ildLinear);
            } else {
                right[i] = 0;
            }
        }
        
        return new float[][] { left, right };
    }
    
    private static void writeStereoWav(float[] left, float[] right, float sampleRate, File outFile) throws IOException {
        int nFrames = Math.min(left.length, right.length);
        byte[] audioBytes = new byte[nFrames * 4]; // 2 channels x 16-bit
        
        for (int i = 0; i < nFrames; i++) {
            int l = (int) Math.max(Math.min(left[i] * 32767.0, 32767), -32768);
            int r = (int) Math.max(Math.min(right[i] * 32767.0, 32767), -32768);
            audioBytes[i * 4] = (byte) (l & 0xFF);
            audioBytes[i * 4 + 1] = (byte) ((l >> 8) & 0xFF);
            audioBytes[i * 4 + 2] = (byte) (r & 0xFF);
            audioBytes[i * 4 + 3] = (byte) ((r >> 8) & 0xFF);
        }
        
        AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audioBytes), format, nFrames)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
        }
    }
} 