import muse2.*;
import javax.sound.sampled.*;
import java.io.*;

public class GenerateTestFiles {
    public static void main(String[] args) {
        System.out.println("Generating test audio files for MUSE2...");
        
        // Create input directory
        File inputDir = new File("input_wavs");
        if (!inputDir.exists()) {
            inputDir.mkdir();
        }
        
        float sampleRate = 44100.0f;
        
        // Generate different test files
        generateTestFile("test_center.wav", sampleRate, 1000.0f, 0.0, 0.0); // Center
        generateTestFile("test_left.wav", sampleRate, 1000.0f, 500.0, 6.0); // Left
        generateTestFile("test_right.wav", sampleRate, 1000.0f, -500.0, -6.0); // Right
        generateTestFile("test_speech.wav", sampleRate, 800.0f, 200.0, 2.0); // Speech-like
        
        System.out.println("✅ Test files generated in 'input_wavs' folder:");
        System.out.println("  - test_center.wav (center, no spatial cues)");
        System.out.println("  - test_left.wav (left side)");
        System.out.println("  - test_right.wav (right side)");
        System.out.println("  - test_speech.wav (speech-like, slight left)");
        System.out.println("\nNow you can run: java -cp bin muse2.SimpleMain input_wavs results.csv");
    }
    
    private static void generateTestFile(String filename, float sampleRate, float freq, double itdUs, double ildDb) {
        try {
            // Generate stereo signal
            float[][] stereo = SyntheticSignalGenerator.generateSineStereo(2.0, sampleRate, freq, itdUs, ildDb);
            
            // Write to WAV file
            File outFile = new File("input_wavs", filename);
            writeStereoWav(stereo[0], stereo[1], sampleRate, outFile);
            
        } catch (Exception e) {
            System.err.println("Error generating " + filename + ": " + e.getMessage());
        }
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