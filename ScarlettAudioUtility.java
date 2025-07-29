import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class ScarlettAudioUtility {
    
    public static void main(String[] args) {
        System.out.println("=== Scarlett Audio Interface Utility ===");
        
        // List all available audio devices
        listAudioDevices();
        
        // Test Scarlett connection
        testScarlettConnection();
        
        // Generate test tones
        generateTestTones();
        
        System.out.println("\n✅ Scarlett utility completed!");
    }
    
    private static void listAudioDevices() {
        System.out.println("\n🔍 Available Audio Devices:");
        System.out.println("==========================");
        
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixers.length; i++) {
            Mixer.Info mixer = mixers[i];
            System.out.printf("%d. %s\n", i + 1, mixer.getName());
            System.out.printf("   Description: %s\n", mixer.getDescription());
            System.out.printf("   Vendor: %s\n", mixer.getVendor());
            System.out.printf("   Version: %s\n", mixer.getVersion());
            
            // Check if it's a Scarlett
            if (mixer.getName().toLowerCase().contains("focusrite") || 
                mixer.getName().toLowerCase().contains("scarlett")) {
                System.out.println("   ✅ SCARLETT DETECTED!");
            }
            System.out.println();
        }
    }
    
    private static void testScarlettConnection() {
        System.out.println("🎧 Testing Scarlett Connection:");
        System.out.println("===============================");
        
        Mixer.Info scarlettMixer = findScarlettMixer();
        
        if (scarlettMixer != null) {
            System.out.println("✅ Scarlett found: " + scarlettMixer.getName());
            
            try {
                Mixer mixer = AudioSystem.getMixer(scarlettMixer);
                System.out.println("✅ Scarlett mixer accessible");
                
                // Test line support
                AudioFormat testFormat = new AudioFormat(44100, 16, 2, true, false);
                DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, testFormat);
                
                if (mixer.isLineSupported(lineInfo)) {
                    System.out.println("✅ Scarlett supports 44.1kHz stereo output");
                } else {
                    System.out.println("⚠️  Scarlett may not support 44.1kHz stereo");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error accessing Scarlett: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Scarlett not found");
            System.out.println("💡 Make sure:");
            System.out.println("   - Scarlett is connected via USB");
            System.out.println("   - Scarlett drivers are installed");
            System.out.println("   - Scarlett is set as default audio device");
        }
    }
    
    private static Mixer.Info findScarlettMixer() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixer : mixers) {
            if (mixer.getName().toLowerCase().contains("focusrite") || 
                mixer.getName().toLowerCase().contains("scarlett")) {
                return mixer;
            }
        }
        return null;
    }
    
    private static void generateTestTones() {
        System.out.println("🔊 Generating Test Tones:");
        System.out.println("=========================");
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Test tones will help verify audio quality:");
        System.out.println("1. 1kHz sine wave (standard test tone)");
        System.out.println("2. Left/Right channel test");
        System.out.println("3. ITD test (left vs right timing)");
        System.out.println("4. ILD test (left vs right level)");
        
        System.out.print("\nGenerate test tones? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (response.equals("y") || response.equals("yes")) {
            try {
                // Generate test tones
                generateSineTone(1000, 2.0f, "test_1khz.wav"); // 1kHz, 2 seconds
                generateLeftRightTest("test_left_right.wav");
                generateITDTest("test_itd.wav");
                generateILDTest("test_ild.wav");
                
                System.out.println("✅ Test tones generated:");
                System.out.println("   - test_1khz.wav (1kHz sine wave)");
                System.out.println("   - test_left_right.wav (L/R channel test)");
                System.out.println("   - test_itd.wav (timing difference test)");
                System.out.println("   - test_ild.wav (level difference test)");
                
                System.out.println("\n🎧 Play these through your Scarlett to verify:");
                System.out.println("   - test_1khz.wav should be a clear tone");
                System.out.println("   - test_left_right.wav should alternate L/R");
                System.out.println("   - test_itd.wav should sound left/right");
                System.out.println("   - test_ild.wav should have level differences");
                
            } catch (Exception e) {
                System.err.println("❌ Error generating test tones: " + e.getMessage());
            }
        }
    }
    
    private static void generateSineTone(float frequency, float duration, String filename) throws Exception {
        float sampleRate = 44100;
        int numSamples = (int) (sampleRate * duration);
        
        byte[] audioData = new byte[numSamples * 4]; // 16-bit stereo
        
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            short sample = (short) (Math.sin(2 * Math.PI * frequency * t) * 8000);
            
            // Left channel
            audioData[i * 4] = (byte) (sample & 0xFF);
            audioData[i * 4 + 1] = (byte) ((sample >> 8) & 0xFF);
            
            // Right channel (same)
            audioData[i * 4 + 2] = (byte) (sample & 0xFF);
            audioData[i * 4 + 3] = (byte) ((sample >> 8) & 0xFF);
        }
        
        writeWavFile(filename, audioData, sampleRate, 2);
    }
    
    private static void generateLeftRightTest(String filename) throws Exception {
        float sampleRate = 44100;
        float duration = 3.0f;
        int numSamples = (int) (sampleRate * duration);
        
        byte[] audioData = new byte[numSamples * 4];
        
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            short sample = (short) (Math.sin(2 * Math.PI * 800 * t) * 8000);
            
            // Left channel only for first second
            if (t < 1.0) {
                audioData[i * 4] = (byte) (sample & 0xFF);
                audioData[i * 4 + 1] = (byte) ((sample >> 8) & 0xFF);
                audioData[i * 4 + 2] = 0; // Right silent
                audioData[i * 4 + 3] = 0;
            }
            // Right channel only for second second
            else if (t < 2.0) {
                audioData[i * 4] = 0; // Left silent
                audioData[i * 4 + 1] = 0;
                audioData[i * 4 + 2] = (byte) (sample & 0xFF);
                audioData[i * 4 + 3] = (byte) ((sample >> 8) & 0xFF);
            }
            // Both channels for third second
            else {
                audioData[i * 4] = (byte) (sample & 0xFF);
                audioData[i * 4 + 1] = (byte) ((sample >> 8) & 0xFF);
                audioData[i * 4 + 2] = (byte) (sample & 0xFF);
                audioData[i * 4 + 3] = (byte) ((sample >> 8) & 0xFF);
            }
        }
        
        writeWavFile(filename, audioData, sampleRate, 2);
    }
    
    private static void generateITDTest(String filename) throws Exception {
        float sampleRate = 44100;
        float duration = 2.0f;
        int numSamples = (int) (sampleRate * duration);
        
        byte[] audioData = new byte[numSamples * 4];
        int delaySamples = 500; // ~11ms delay
        
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            short sample = (short) (Math.sin(2 * Math.PI * 1000 * t) * 8000);
            
            // Left channel
            audioData[i * 4] = (byte) (sample & 0xFF);
            audioData[i * 4 + 1] = (byte) ((sample >> 8) & 0xFF);
            
            // Right channel (delayed)
            int rightIndex = i - delaySamples;
            if (rightIndex >= 0) {
                audioData[i * 4 + 2] = (byte) (sample & 0xFF);
                audioData[i * 4 + 3] = (byte) ((sample >> 8) & 0xFF);
            } else {
                audioData[i * 4 + 2] = 0;
                audioData[i * 4 + 3] = 0;
            }
        }
        
        writeWavFile(filename, audioData, sampleRate, 2);
    }
    
    private static void generateILDTest(String filename) throws Exception {
        float sampleRate = 44100;
        float duration = 2.0f;
        int numSamples = (int) (sampleRate * duration);
        
        byte[] audioData = new byte[numSamples * 4];
        
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            short leftSample = (short) (Math.sin(2 * Math.PI * 1000 * t) * 8000);
            short rightSample = (short) (Math.sin(2 * Math.PI * 1000 * t) * 4000); // Half level
            
            // Left channel (full level)
            audioData[i * 4] = (byte) (leftSample & 0xFF);
            audioData[i * 4 + 1] = (byte) ((leftSample >> 8) & 0xFF);
            
            // Right channel (half level)
            audioData[i * 4 + 2] = (byte) (rightSample & 0xFF);
            audioData[i * 4 + 3] = (byte) ((rightSample >> 8) & 0xFF);
        }
        
        writeWavFile(filename, audioData, sampleRate, 2);
    }
    
    private static void writeWavFile(String filename, byte[] audioData, float sampleRate, int channels) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
             FileOutputStream fos = new FileOutputStream(filename)) {
            
            // Write WAV header
            byte[] header = createWavHeader(audioData.length, format);
            fos.write(header);
            
            // Write audio data
            fos.write(audioData);
        }
    }
    
    private static byte[] createWavHeader(int dataLength, AudioFormat format) {
        byte[] header = new byte[44];
        
        // RIFF header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        int fileSize = dataLength + 36;
        header[4] = (byte) (fileSize & 0xFF);
        header[5] = (byte) ((fileSize >> 8) & 0xFF);
        header[6] = (byte) ((fileSize >> 16) & 0xFF);
        header[7] = (byte) ((fileSize >> 24) & 0xFF);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        
        // fmt chunk
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0; // fmt chunk size
        header[20] = 1; header[21] = 0; // PCM format
        header[22] = (byte) format.getChannels(); header[23] = 0; // channels
        int sampleRateInt = (int) format.getSampleRate();
        header[24] = (byte) (sampleRateInt & 0xFF);
        header[25] = (byte) ((sampleRateInt >> 8) & 0xFF);
        header[26] = (byte) ((sampleRateInt >> 16) & 0xFF);
        header[27] = (byte) ((sampleRateInt >> 24) & 0xFF);
        int byteRate = (int) (format.getSampleRate() * format.getChannels() * format.getSampleSizeInBits() / 8);
        header[28] = (byte) (byteRate & 0xFF);
        header[29] = (byte) ((byteRate >> 8) & 0xFF);
        header[30] = (byte) ((byteRate >> 16) & 0xFF);
        header[31] = (byte) ((byteRate >> 24) & 0xFF);
        header[32] = (byte) (format.getChannels() * format.getSampleSizeInBits() / 8); header[33] = 0; // block align
        header[34] = (byte) format.getSampleSizeInBits(); header[35] = 0; // bits per sample
        
        // data chunk
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (dataLength & 0xFF);
        header[41] = (byte) ((dataLength >> 8) & 0xFF);
        header[42] = (byte) ((dataLength >> 16) & 0xFF);
        header[43] = (byte) ((dataLength >> 24) & 0xFF);
        
        return header;
    }
} 