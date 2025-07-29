import javax.sound.sampled.*;
import java.io.*;

public class AudioTest {
    public static void main(String[] args) {
        System.out.println("=== Audio Playback Test ===");
        System.out.println("This will test if audio playback works on your system.");
        
        // Check if we have any audio files to test with
        File stimuliDir = new File("output_stimuli");
        File[] audioFiles = stimuliDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        
        if (audioFiles == null || audioFiles.length == 0) {
            System.out.println("❌ No audio files found in output_stimuli/");
            System.out.println("Please run the main system first to generate audio files.");
            return;
        }
        
        System.out.println("✅ Found " + audioFiles.length + " audio files");
        System.out.println("Testing with: " + audioFiles[0].getName());
        
        System.out.println("\nInstructions:");
        System.out.println("1. Connect headphones to your computer");
        System.out.println("2. Put headphones on the participant");
        System.out.println("3. Press Enter to play a test sound");
        
        try {
            System.in.read();
            
            System.out.println("Playing test audio...");
            playAudioFile(audioFiles[0]);
            
            System.out.println("✅ Audio test complete!");
            System.out.println("If you heard the sound, audio is working correctly.");
            System.out.println("If not, check your audio settings and headphones.");
            
        } catch (Exception e) {
            System.err.println("❌ Audio test failed: " + e.getMessage());
            System.out.println("Check your audio settings and try again.");
        }
    }
    
    private static void playAudioFile(File audioFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            
            System.out.println("Audio playing... (waiting for it to finish)");
            
            // Wait for audio to finish
            while (clip.isActive()) {
                Thread.sleep(100);
            }
            
            clip.close();
            audioStream.close();
            
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
        }
    }
} 