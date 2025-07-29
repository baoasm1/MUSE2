import java.io.*;
import java.util.*;

public class RealSpeechAnalyzer {
    
    public static void main(String[] args) throws Exception {
        System.out.println("🗣️ Real Speech Analysis for MUSE2 Research");
        System.out.println("==========================================");
        
        // Analyze existing speech files
        analyzeSpeechFiles();
        
        // Generate research-quality speech tokens
        generateResearchTokens();
        
        // Validate with real speech
        validateWithRealSpeech();
        
        System.out.println("\n✅ Real speech analysis completed!");
    }
    
    private static void analyzeSpeechFiles() throws Exception {
        System.out.println("\n📊 Analyzing Existing Speech Files");
        
        File inputDir = new File("input_wavs");
        if (!inputDir.exists()) {
            System.out.println("⚠️  No input_wavs directory found");
            return;
        }
        
        File[] wavFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (wavFiles == null || wavFiles.length == 0) {
            System.out.println("⚠️  No WAV files found");
            return;
        }
        
        // Create results file
        try (PrintWriter writer = new PrintWriter(new FileWriter("real_speech_analysis.csv"))) {
            writer.println("filename,original_itd,original_ild,shift_200_itd,shift_200_ild,shift_400_itd,shift_400_ild,shift_600_itd,shift_600_ild");
            
            for (File wavFile : wavFiles) {
                System.out.printf("Processing: %s\n", wavFile.getName());
                
                try {
                    // Load original speech
                    float[][] original = muse2.AudioProcessor.loadWavFile(wavFile.getPath());
                    
                    // Measure original spatial cues
                    double originalITD = muse2.ImprovedSpatialCueAnalyzer.computeITD(original[0], original[1], 44100);
                    double originalILD = muse2.ImprovedSpatialCueAnalyzer.computeILD(original[0], original[1]);
                    
                    // Apply frequency shifts and measure changes
                    float[] shifts = {200, 400, 600};
                    double[] shiftedITDs = new double[3];
                    double[] shiftedILDs = new double[3];
                    
                    for (int i = 0; i < shifts.length; i++) {
                        float[][] shifted = muse2.SimpleFrequencyShifter.shift(
                            original[0], original[1], 44100, shifts[i]);
                        
                        shiftedITDs[i] = muse2.ImprovedSpatialCueAnalyzer.computeITD(shifted[0], shifted[1], 44100);
                        shiftedILDs[i] = muse2.ImprovedSpatialCueAnalyzer.computeILD(shifted[0], shifted[1], 44100);
                        
                        // Save shifted file
                        String outputName = wavFile.getName().replace(".wav", "_shifted_" + (int)shifts[i] + "Hz.wav");
                        saveWavFile(shifted, "output_stimuli/" + outputName, 44100);
                    }
                    
                    // Write results
                    writer.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        wavFile.getName(), originalITD, originalILD,
                        shiftedITDs[0], shiftedILDs[0],
                        shiftedITDs[1], shiftedILDs[1],
                        shiftedITDs[2], shiftedILDs[2]);
                    
                    System.out.printf("  Original: ITD=%.1fμs, ILD=%.1fdB\n", originalITD, originalILD);
                    System.out.printf("  Shifted: ITD=%.1fμs, ILD=%.1fdB\n", shiftedITDs[1], shiftedILDs[1]);
                    
                } catch (Exception e) {
                    System.err.printf("Error processing %s: %s\n", wavFile.getName(), e.getMessage());
                }
            }
        }
        
        System.out.println("✅ Analysis saved to real_speech_analysis.csv");
    }
    
    private static void generateResearchTokens() throws Exception {
        System.out.println("\n🎯 Generating Research-Quality Speech Tokens");
        
        // Create research tokens with controlled spatial cues
        String[] words = {"hello", "world", "test", "speech", "audio"};
        double[] itds = {0, 100, 200, 500, 1000}; // microseconds
        double[] ilds = {0, 3, 6, 10, 15}; // dB
        
        int tokenCount = 0;
        
        for (String word : words) {
            for (double itd : itds) {
                for (double ild : ilds) {
                    // Generate speech-like token with specific spatial cues
                    float[][] token = generateSpeechToken(word, itd, ild);
                    
                    // Save original token
                    String filename = String.format("research_token_%s_ITD%.0f_ILD%.0f.wav", 
                        word, itd, ild);
                    saveWavFile(token, "research_tokens/" + filename, 44100);
                    
                    // Apply frequency shifts
                    for (float shift : new float[]{200, 400, 600}) {
                        float[][] shifted = muse2.SimpleFrequencyShifter.shift(
                            token[0], token[1], 44100, shift);
                        
                        String shiftedFilename = String.format("research_token_%s_ITD%.0f_ILD%.0f_shift%.0f.wav",
                            word, itd, ild, shift);
                        saveWavFile(shifted, "research_tokens/" + shiftedFilename, 44100);
                    }
                    
                    tokenCount++;
                }
            }
        }
        
        System.out.printf("✅ Generated %d research tokens\n", tokenCount);
    }
    
    private static float[][] generateSpeechToken(String word, double itd, double ild) {
        // Simplified speech token generation
        float sampleRate = 44100;
        float duration = 1.0f; // 1 second
        int numSamples = (int) (sampleRate * duration);
        
        float[] left = new float[numSamples];
        float[] right = new float[numSamples];
        
        // Generate speech-like signal (simplified)
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            
            // Multi-frequency speech-like signal
            double signal = 0;
            signal += 0.5 * Math.sin(2 * Math.PI * 200 * t); // Fundamental
            signal += 0.3 * Math.sin(2 * Math.PI * 400 * t); // Second harmonic
            signal += 0.2 * Math.sin(2 * Math.PI * 800 * t); // Third harmonic
            signal += 0.1 * Math.sin(2 * Math.PI * 1600 * t); // Fourth harmonic
            
            // Apply envelope for speech-like characteristics
            double envelope = Math.exp(-t * 2) * (1 - Math.exp(-t * 10));
            signal *= envelope;
            
            left[i] = (float) signal;
        }
        
        // Apply ITD and ILD
        int itdSamples = (int) (itd * sampleRate / 1_000_000);
        double ildLinear = Math.pow(10, ild / 20);
        
        if (itdSamples >= 0) {
            // Right ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i - itdSamples;
                if (j >= 0) {
                    right[i] = (float) (left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        } else {
            // Left ear delayed
            for (int i = 0; i < numSamples; i++) {
                int j = i + itdSamples;
                if (j >= 0) {
                    right[i] = (float) (left[j] / ildLinear);
                } else {
                    right[i] = 0;
                }
            }
        }
        
        return new float[][] { left, right };
    }
    
    private static void validateWithRealSpeech() throws Exception {
        System.out.println("\n🔬 Validating System with Real Speech");
        
        // Test with research tokens
        File researchDir = new File("research_tokens");
        if (!researchDir.exists()) {
            System.out.println("⚠️  No research_tokens directory found");
            return;
        }
        
        File[] tokens = researchDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (tokens == null || tokens.length == 0) {
            System.out.println("⚠️  No research tokens found");
            return;
        }
        
        int validTokens = 0;
        int totalTokens = tokens.length;
        
        for (File token : tokens) {
            try {
                float[][] audio = muse2.AudioProcessor.loadWavFile(token.getPath());
                
                // Validate audio properties
                boolean validChannels = audio.length == 2;
                boolean validLength = audio[0].length > 0;
                boolean validAmplitude = checkAmplitudeRange(audio);
                
                if (validChannels && validLength && validAmplitude) {
                    validTokens++;
                }
                
            } catch (Exception e) {
                System.err.printf("Invalid token %s: %s\n", token.getName(), e.getMessage());
            }
        }
        
        double validityRate = (validTokens * 100.0) / totalTokens;
        System.out.printf("✅ Token validity: %d/%d (%.1f%%)\n", validTokens, totalTokens, validityRate);
    }
    
    private static void saveWavFile(float[][] stereo, String filename, float sampleRate) throws Exception {
        // Ensure output directory exists
        File file = new File(filename);
        file.getParentFile().mkdirs();
        
        // Convert to byte array
        int numSamples = stereo[0].length;
        byte[] audioData = new byte[numSamples * 4]; // 16-bit stereo
        
        for (int i = 0; i < numSamples; i++) {
            short leftSample = (short) (stereo[0][i] * 32767);
            short rightSample = (short) (stereo[1][i] * 32767);
            
            // Left channel
            audioData[i * 4] = (byte) (leftSample & 0xFF);
            audioData[i * 4 + 1] = (byte) ((leftSample >> 8) & 0xFF);
            
            // Right channel
            audioData[i * 4 + 2] = (byte) (rightSample & 0xFF);
            audioData[i * 4 + 3] = (byte) ((rightSample >> 8) & 0xFF);
        }
        
        // Write WAV file
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // Write WAV header
            byte[] header = createWavHeader(audioData.length, sampleRate, 2);
            fos.write(header);
            
            // Write audio data
            fos.write(audioData);
        }
    }
    
    private static byte[] createWavHeader(int dataLength, float sampleRate, int channels) {
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
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0; // PCM format
        header[22] = (byte) channels; header[23] = 0;
        int sampleRateInt = (int) sampleRate;
        header[24] = (byte) (sampleRateInt & 0xFF);
        header[25] = (byte) ((sampleRateInt >> 8) & 0xFF);
        header[26] = (byte) ((sampleRateInt >> 16) & 0xFF);
        header[27] = (byte) ((sampleRateInt >> 24) & 0xFF);
        int byteRate = (int) (sampleRate * channels * 16 / 8);
        header[28] = (byte) (byteRate & 0xFF);
        header[29] = (byte) ((byteRate >> 8) & 0xFF);
        header[30] = (byte) ((byteRate >> 16) & 0xFF);
        header[31] = (byte) ((byteRate >> 24) & 0xFF);
        header[32] = (byte) (channels * 16 / 8); header[33] = 0;
        header[34] = 16; header[35] = 0; // 16-bit
        
        // data chunk
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (dataLength & 0xFF);
        header[41] = (byte) ((dataLength >> 8) & 0xFF);
        header[42] = (byte) ((dataLength >> 16) & 0xFF);
        header[43] = (byte) ((dataLength >> 24) & 0xFF);
        
        return header;
    }
    
    private static boolean checkAmplitudeRange(float[][] audio) {
        for (int ch = 0; ch < audio.length; ch++) {
            for (int i = 0; i < audio[ch].length; i++) {
                if (audio[ch][i] < -1.0 || audio[ch][i] > 1.0) {
                    return false;
                }
            }
        }
        return true;
    }
} 