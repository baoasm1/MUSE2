import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class AdvancedSpatialTest {
    private static final String CSV_HEADER = "participant_id,timestamp,trial_number,stimulus,itd_us,ild_db,frequency_shift,response,response_time_ms,correct,condition";
    private static final Random random = new Random();
    
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Advanced Spatial Cue Research Test ===");
        
        // Participant information
        System.out.print("Enter participant ID: ");
        String participantId = scanner.nextLine().trim();
        
        System.out.print("Enter participant age: ");
        int age = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter participant gender (M/F/O): ");     String gender = scanner.nextLine().trim().toUpperCase();
        
        // Test configuration
        System.out.print("Enter test type (basic/adaptive/threshold): ");       String testType = scanner.nextLine().trim().toLowerCase();
        
        System.out.print("Enter number of trials per condition: ");
        int trialsPerCondition = Integer.parseInt(scanner.nextLine().trim());
        
        // Stimuli folder
        System.out.print("Enter folder with test WAV files: ");     String folderPath = scanner.nextLine().trim();
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("❌ Folder not found: " + folderPath);
            return;
        }
        
        // Initialize test session
        TestSession session = new TestSession(participantId, age, gender, testType, trialsPerCondition);
        session.loadStimuli(folder);
        
        // Run the test
        runTest(session, scanner);
        
        // Save results
        session.saveResults();
        
        System.out.println("\n🎉 Test completed! Results saved to: " + session.getResultsFile());
        System.out.println("📊 Summary: " + session.getSummary());
    }
    
    private static void runTest(TestSession session, Scanner scanner) throws Exception {
        System.out.println("\n=== Test Instructions ===");
        System.out.println("You will hear sounds through headphones.");
        System.out.println("For each sound, indicate:");
        System.out.println("  L = Left side");
        System.out.println("  R = Right side");
        System.out.println("  C = Center (if applicable)");
        System.out.println("  ? = Unsure");
        System.out.println("\nPress Enter when ready to begin...");
        scanner.nextLine();
        
        // Run trials
        for (int trial = 1; trial <= session.getTotalTrials(); trial++) {
            Stimulus stimulus = session.getNextStimulus();
            
            System.out.printf("\n--- Trial %d/%d ---\n", trial, session.getTotalTrials());
            System.out.printf("Playing: %s (ITD: %.1fμs, ILD: %.1fdB)\n", 
                stimulus.filename, stimulus.itd, stimulus.ild);
            
            long startTime = System.currentTimeMillis();
            playWav(stimulus.file);
            
            String response = "";
            while (true) {
                System.out.print("Response (L/R/C/?): ");
                response = scanner.nextLine().trim().toUpperCase();
                if (response.matches("[LRC?]")) {
                    break;
                }
                System.out.println("Please type L, R, C, or ?");
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            boolean correct = session.checkResponse(stimulus, response);
            
            session.recordTrial(trial, stimulus, response, responseTime, correct);
            
            // Provide feedback (optional)
            if (session.getTestType().equals("basic")) {
                System.out.printf("Response: %s (%.1fμs, %.1fdB, %.1fHz, %s)\n", 
                    response, stimulus.itd, stimulus.ild, stimulus.frequencyShift, correct ? "✅ Correct" : "❌ Incorrect");
            }
        }
    }
    
    private static void playWav(File wavFile) {
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(wavFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            while (clip.isRunning()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            clip.close();
        } catch (Exception e) {
            System.err.println("Error playing " + wavFile.getName() + ": " + e.getMessage());
        }
    }
    
    // Stimulus class
    static class Stimulus {
        File file;
        String filename;
        double itd, ild, frequencyShift;
        String condition;
        
        Stimulus(File file, double itd, double ild, double freqShift, String condition) {
            this.file = file;
            this.filename = file.getName();
            this.itd = itd;
            this.ild = ild;
            this.frequencyShift = freqShift;
            this.condition = condition;
        }
    }
    
    // Test session class
    static class TestSession {
        private String participantId, gender, testType;
        private int age, trialsPerCondition;
        private List<Stimulus> stimuli = new ArrayList<>();
        private List<String> results = new ArrayList<>();
        private int currentTrial = 0;
        private SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        TestSession(String pid, int age, String gender, String testType, int trials) {
            this.participantId = pid;
            this.age = age;
            this.gender = gender;
            this.testType = testType;
            this.trialsPerCondition = trials;
            results.add(CSV_HEADER);
        }
        
        void loadStimuli(File folder) {
            File[] wavFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (wavFiles == null) return;
            
            for (File wav : wavFiles) {
                // Parse stimulus parameters from filename
                // Expected format: stimulus_ITD50av
                double itd = parseITD(wav.getName());
                double ild = parseILD(wav.getName());
                double freqShift = parseFreqShift(wav.getName());
                String condition = parseCondition(wav.getName());
                
                stimuli.add(new Stimulus(wav, itd, ild, freqShift, condition));
            }
            
            // Randomize stimulus order
            Collections.shuffle(stimuli, random);
            
            System.out.printf("Loaded %d stimuli\n", stimuli.size());
        }
        
        Stimulus getNextStimulus() {
            if (currentTrial >= stimuli.size()) {
                // Repeat stimuli if needed
                Collections.shuffle(stimuli, random);
                currentTrial = 0;
            }
            return stimuli.get(currentTrial++);
        }
        
        int getTotalTrials() {
            return Math.max(trialsPerCondition, stimuli.size());
        }
        
        boolean checkResponse(Stimulus stimulus, String response) {
            // Determine correct response based on ITD/ILD
            String correctResponse = "C"; // Default to center
            
            if (Math.abs(stimulus.itd) > 100) { // Significant ITD
                correctResponse = stimulus.itd > 0 ? "R" : "L";
            } else if (Math.abs(stimulus.ild) > 3) { // Significant ILD
                correctResponse = stimulus.ild > 0 ? "L" : "R";
            }
            
            return response.equals(correctResponse);
        }
        
        void recordTrial(int trialNum, Stimulus stimulus, String response, long responseTime, boolean correct) {
            String record = String.format("%s,%s,%d,%s,%.1f,%.1f,%0.1f,%s,%d,%s", 
                participantId, timestamp.format(new Date()), trialNum, stimulus.filename,
                stimulus.itd, stimulus.ild, stimulus.frequencyShift, response, responseTime,
                correct ? "1" : "0", stimulus.condition);
            results.add(record);
        }
        
        void saveResults() throws IOException {
            String filename = String.format("advanced_results_%s_%s.csv", 
                participantId, timestamp.format(new Date()).replace(":", "-"));      
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                for (String line : results) {
                    writer.println(line);
                }
            }
        }
        
        String getResultsFile() {
            return String.format("advanced_results_%s_%s.csv", 
                participantId, timestamp.format(new Date()).replace(":", "-"));      
        }
        
        String getSummary() {
            long totalTrials = results.size() - 1; // Exclude header
            long correctTrials = results.stream().skip(1)
                .mapToLong(line -> line.endsWith(",1,") ? 1 : 0).sum();
            
            return String.format("%d/%d correct (%.1f%%)", 
                correctTrials, totalTrials, (correctTrials * 100.0 / totalTrials));
        }
        
        String getTestType() { return testType; }
        
        // Helper methods to parse stimulus parameters from filename
        private double parseITD(String filename) {
            // Extract ITD value from filename
            if (filename.contains("ITD")) {
                String[] parts = filename.split("_");
                for (String part : parts) {
                    if (part.startsWith("ITD")) {
                        return Double.parseDouble(part.substring(3));
                    }
                }
            }
            return 0.0;
        }
        
        private double parseILD(String filename) {
            if (filename.contains("ILD")) {
                String[] parts = filename.split("_");
                for (String part : parts) {
                    if (part.startsWith("ILD")) {
                        return Double.parseDouble(part.substring(3));
                    }
                }
            }
            return 0.0;
        }
        
        private double parseFreqShift(String filename) {
            if (filename.contains("FREQ")) {
                String[] parts = filename.split("_");
                for (String part : parts) {
                    if (part.startsWith("FREQ")) {
                        return Double.parseDouble(part.substring(4));
                    }
                }
            }
            return 0.0;
        }
        
        private String parseCondition(String filename) {
            if (filename.contains("SHIFT")) return "frequency_shifted";
            if (filename.contains("ORIG")) return "original";
            return "unknown";
        }
    }
} 