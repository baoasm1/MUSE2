import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class TestInterface {
    private static final String STIMULI_DIR = "output_stimuli";
    private static final String RESULTS_FILE = "participant_responses.csv";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== MUSE2 Spatial Hearing Test ===");
        System.out.println("This test will play audio files and ask you to localize sounds.");
        System.out.println("Press Enter when ready to begin...");
        scanner.nextLine();
        
        // Get participant info
        System.out.print("Enter participant ID: ");
        String participantId = scanner.nextLine();
        
        // Get all shifted audio files
        File stimuliDir = new File(STIMULI_DIR);
        File[] audioFiles = stimuliDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        
        if (audioFiles == null || audioFiles.length == 0) {
            System.out.println("No audio files found in " + STIMULI_DIR);
            return;
        }
        
        // Randomize order
        List<File> randomizedFiles = Arrays.asList(audioFiles);
        Collections.shuffle(randomizedFiles);
        
        System.out.println("\nYou will hear " + randomizedFiles.size() + " audio files.");
        System.out.println("For each file, indicate where you think the sound is coming from:");
        System.out.println("1 = Far Left, 2 = Left, 3 = Center, 4 = Right, 5 = Far Right");
        System.out.println("Press Enter to start...");
        scanner.nextLine();
        
        List<String> responses = new ArrayList<>();
        
        for (int i = 0; i < randomizedFiles.size(); i++) {
            File audioFile = randomizedFiles.get(i);
            System.out.println("\n--- Trial " + (i + 1) + " of " + randomizedFiles.size() + " ---");
            System.out.println("Playing: " + audioFile.getName());
            
            // Play audio
            playAudioFile(audioFile);
            
            // Get response
            System.out.print("Where did the sound come from? (1-5): ");
            String response = scanner.nextLine();
            
            // Store response
            responses.add(participantId + "," + audioFile.getName() + "," + response);
            
            System.out.println("Response recorded.");
        }
        
        // Save responses
        saveResponses(responses);
        
        System.out.println("\n=== Test Complete ===");
        System.out.println("Thank you for participating!");
        System.out.println("Responses saved to: " + RESULTS_FILE);
        
        scanner.close();
    }
    
    private static void playAudioFile(File audioFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            
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
    
    private static void saveResponses(List<String> responses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTS_FILE, true))) {
            // Write header if file is empty
            if (new File(RESULTS_FILE).length() == 0) {
                writer.println("participant_id,audio_file,localization_response");
            }
            
            // Write responses
            for (String response : responses) {
                writer.println(response);
            }
            
        } catch (IOException e) {
            System.err.println("Error saving responses: " + e.getMessage());
        }
    }
} 