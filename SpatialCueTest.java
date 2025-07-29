import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class SpatialCueTest {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Spatial Cue Participant Test ===");
        System.out.print("Enter participant ID: ");
        String participantId = scanner.nextLine().trim();

        // Folder with test stimuli (WAV files)
        System.out.print("Enter folder with test WAV files: ");
        String folderPath = scanner.nextLine().trim();
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("❌ Folder not found: " + folderPath);
            return;
        }

        // List all .wav files
        File[] wavFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (wavFiles == null || wavFiles.length == 0) {
            System.err.println("❌ No WAV files found in folder.");
            return;
        }
        Arrays.sort(wavFiles);

        // Prepare CSV output
        String csvName = "responses_" + participantId + ".csv";
        PrintWriter csv = new PrintWriter(new FileWriter(csvName));
        csv.println("participant_id,stimulus,response");

        System.out.println("\nInstructions:");
        System.out.println("- You will hear a series of sounds through headphones.");
        System.out.println("- After each sound, indicate if it was on the Left (L) or Right (R).");
        System.out.println("- Type L or R and press Enter after each sound.\n");
        System.out.println("Press Enter to begin...");
        scanner.nextLine();

        for (File wav : wavFiles) {
            System.out.println("Playing: " + wav.getName());
            playWav(wav);
            String response = "";
            while (true) {
                System.out.print("Where did the sound come from? (L/R): ");
                response = scanner.nextLine().trim().toUpperCase();
                if (response.equals("L") || response.equals("R")) break;
                System.out.println("Please type L or R.");
            }
            csv.printf("%s,%s,%s\n", participantId, wav.getName(), response);
        }
        csv.close();
        System.out.println("\nThank you! Responses saved to " + csvName);
    }

    // Simple WAV playback using Java Sound API
    private static void playWav(File wavFile) {
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(wavFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            while (clip.isRunning()) {
                Thread.sleep(50);
            }
            clip.close();
        } catch (Exception e) {
            System.err.println("Error playing " + wavFile.getName() + ": " + e.getMessage());
        }
    }
} 