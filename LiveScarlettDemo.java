import javax.sound.sampled.*;
import java.io.*;
import java.util.Scanner;

public class LiveScarlettDemo {
    private static final float SAMPLE_RATE = 44100;
    private static Mixer scarlettMixer;
    private static SourceDataLine outputLine;
    
    public static void main(String[] args) {
        System.out.println("===== MUSE2 Live Scarlett Demo =====");
        System.out.println("This demo shows real-time frequency shifting effects");
        System.out.println("on spatial hearing cues using the Scarlett interface.");
        
        try {
            // Find and initialize Scarlett interface
            initializeScarlett();
            
            // Get input file
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nSelect audio file to use:");
            System.out.println("1. Speech sample (synthetic)");
            System.out.println("2. Music sample (synthetic)");
            System.out.println("3. Environmental sounds (synthetic)");
            System.out.print("Enter choice (1-3): ");
            int fileChoice = scanner.nextInt();
            
            // Get audio file based on choice
            String inputFile = getAudioFile(fileChoice);
            
            // Main demo loop
            boolean running = true;
            while (running) {
                // Display menu
                System.out.println("\n===== Frequency Shifting Demo =====");
                System.out.println("1. Play original (unshifted) audio");
                System.out.println("2. Play with 200Hz shift (safe zone)");
                System.out.println("3. Play with 400Hz shift (moderate)");
                System.out.println("4. Play with 600Hz shift (significant)");
                System.out.println("5. Real-time spatial cue analysis");
                System.out.println("6. Change audio file");
                System.out.println("7. Exit demo");
                System.out.print("Enter choice: ");
                
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        playOriginal(inputFile);
                        break;
                    case 2:
                        playShifted(inputFile, 200);
                        break;
                    case 3:
                        playShifted(inputFile, 400);
                        break;
                    case 4:
                        playShifted(inputFile, 600);
                        break;
                    case 5:
                        analyzeSpatialCues(inputFile);
                        break;
                    case 6:
                        System.out.print("Enter new file choice (1-3): ");
                        fileChoice = scanner.nextInt();
                        inputFile = getAudioFile(fileChoice);
                        break;
                    case 7:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
            System.out.println("Demo completed. Thank you!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void initializeScarlett() throws LineUnavailableException {
        // Find Scarlett mixer
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.println("Looking for Scarlett audio interface...");
        
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().contains("Scarlett") || info.getName().contains("Focusrite")) {
                scarlettMixer = AudioSystem.getMixer(info);
                System.out.println("Found Scarlett interface: " + info.getName());
                break;
            }
        }
        
        if (scarlettMixer == null) {
            System.out.println("Scarlett interface not found. Using default audio device.");
        }
        
        // Set up audio format (stereo, 44.1kHz, 16-bit)
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        
        // Get output line
        outputLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        outputLine.open(format, 4096);
        System.out.println("Audio output initialized successfully.");
    }
    
    private static String getAudioFile(int choice) {
        switch (choice) {
            case 1:
                return "input_wavs/speech_sample.wav";
            case 2:
                return "input_wavs/music_sample.wav";
            case 3:
                return "input_wavs/environment_sample.wav";
            default:
                return "input_wavs/speech_sample.wav";
        }
    }
    
    private static void playOriginal(String inputFile) {
        try {
            System.out.println("Playing original audio...");
            
            // Generate synthetic audio if file doesn't exist
            if (!new File(inputFile).exists()) {
                generateSyntheticAudio(inputFile);
            }
            
            // Load audio file
            muse2.AudioProcessor processor = new muse2.AudioProcessor(inputFile);
            float[] leftChannel = processor.getLeftChannel();
            float[] rightChannel = processor.getRightChannel();
            
            // Play through Scarlett
            playThroughScarlett(leftChannel, rightChannel);
            
            System.out.println("Playback complete.");
            
        } catch (Exception e) {
            System.err.println("Error playing original audio: " + e.getMessage());
        }
    }
    
    private static void playShifted(String inputFile, int shiftAmount) {
        try {
            System.out.println("Playing audio with " + shiftAmount + "Hz shift...");
            
            // Generate synthetic audio if file doesn't exist
            if (!new File(inputFile).exists()) {
                generateSyntheticAudio(inputFile);
            }
            
            // Load and process audio
            muse2.AudioProcessor processor = new muse2.AudioProcessor(inputFile);
            float[] leftChannel = processor.getLeftChannel();
            float[] rightChannel = processor.getRightChannel();
            
            // Apply frequency shifting
            float[][] shifted = muse2.SimpleFrequencyShifter.shift(
                leftChannel, rightChannel, SAMPLE_RATE, shiftAmount);
            
            // Play shifted audio
            playThroughScarlett(shifted[0], shifted[1]);
            
            System.out.println("Shifted playback complete.");
            
        } catch (Exception e) {
            System.err.println("Error playing shifted audio: " + e.getMessage());
        }
    }
    
    private static void analyzeSpatialCues(String inputFile) {
        try {
            System.out.println("Analyzing spatial cues in: " + inputFile);
            
            // Generate synthetic audio if file doesn't exist
            if (!new File(inputFile).exists()) {
                generateSyntheticAudio(inputFile);
            }
            
            // Analyze original
            muse2.SpatialCueAnalyzer.SpatialCues originalCues = 
                muse2.SpatialCueAnalyzer.analyzeStereoFile(inputFile);
            
            System.out.println("Original audio spatial cues:");
            System.out.println("  ITD: " + originalCues.getItdMicroseconds() + "μs");
            System.out.println("  ILD: " + originalCues.getIldDecibels() + "dB");
            
            // Analyze with different shift amounts
            for (int shift : new int[]{200, 400, 600}) {
                System.out.println("\nWith " + shift + "Hz shift:");
                
                // Create shifted audio in memory
                muse2.AudioProcessor processor = new muse2.AudioProcessor(inputFile);
                float[] leftChannel = processor.getLeftChannel();
                float[] rightChannel = processor.getRightChannel();
                
                float[][] shifted = muse2.SimpleFrequencyShifter.shift(
                    leftChannel, rightChannel, SAMPLE_RATE, shift);
                
                // Analyze shifted audio
                muse2.SpatialCueAnalyzer.SpatialCues shiftedCues = 
                    muse2.SpatialCueAnalyzer.analyzeSpatialCues(shifted[0], shifted[1], (int)SAMPLE_RATE);
                
                System.out.println("  ITD: " + shiftedCues.getItdMicroseconds() + "μs");
                System.out.println("  ILD: " + shiftedCues.getIldDecibels() + "dB");
                
                // Show changes
                double itdChange = Math.abs(shiftedCues.getItdMicroseconds() - originalCues.getItdMicroseconds());
                double ildChange = Math.abs(shiftedCues.getIldDecibels() - originalCues.getIldDecibels());
                
                System.out.println("  ITD change: " + itdChange + "μs");
                System.out.println("  ILD change: " + ildChange + "dB");
                
                // Interpret results
                if (itdChange < 10) {
                    System.out.println("  Spatial location preserved (ITD change < 10μs)");
                } else if (itdChange < 20) {
                    System.out.println("  Spatial location slightly affected (ITD change 10-20μs)");
                } else {
                    System.out.println("  Spatial location significantly degraded (ITD change > 20μs)");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error analyzing spatial cues: " + e.getMessage());
        }
    }
    
    private static void generateSyntheticAudio(String filename) {
        try {
            System.out.println("Generating synthetic audio: " + filename);
            
            float[][] audio;
            if (filename.contains("speech")) {
                audio = muse2.SyntheticSignalGenerator.generateSpeechLikeSignal(3.0f, SAMPLE_RATE, 250.0f, 3.0f);
            } else if (filename.contains("music")) {
                audio = muse2.SyntheticSignalGenerator.generateSineStereo(3.0f, SAMPLE_RATE, 1000.0f, 100.0f, 2.0f);
            } else {
                audio = muse2.SyntheticSignalGenerator.generateNoisySignal(3.0f, SAMPLE_RATE, 800.0f, 150.0f, 1.5f, 0.1f);
            }
            
            // Save to WAV file
            saveToWavFile(audio[0], audio[1], filename);
            System.out.println("Synthetic audio generated successfully.");
            
        } catch (Exception e) {
            System.err.println("Error generating synthetic audio: " + e.getMessage());
        }
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
    
    private static void playThroughScarlett(float[] leftChannel, float[] rightChannel) {
        try {
            outputLine.start();
            
            // Convert float audio data to bytes for playback
            byte[] audioBytes = new byte[leftChannel.length * 4]; // 2 bytes per sample, 2 channels
            
            for (int i = 0; i < leftChannel.length; i++) {
                // Convert left channel float to 16-bit
                short leftSample = (short)(leftChannel[i] * 32767.0f);
                audioBytes[i*4] = (byte)(leftSample & 0xFF);
                audioBytes[i*4 + 1] = (byte)((leftSample >> 8) & 0xFF);
                
                // Convert right channel float to 16-bit
                short rightSample = (short)(rightChannel[i] * 32767.0f);
                audioBytes[i*4 + 2] = (byte)(rightSample & 0xFF);
                audioBytes[i*4 + 3] = (byte)((rightSample >> 8) & 0xFF);
            }
            
            // Write to audio line
            outputLine.write(audioBytes, 0, audioBytes.length);
            
            // Wait for playback to complete
            outputLine.drain();
            outputLine.stop();
            
        } catch (Exception e) {
            System.err.println("Error during playback: " + e.getMessage());
        }
    }
} 