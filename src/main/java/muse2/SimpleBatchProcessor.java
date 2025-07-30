package muse2;

import java.io.*;
import java.util.*;

public class SimpleBatchProcessor {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp \".;bin\" muse2.SimpleBatchProcessor <input_directory> <output_csv>");
            System.out.println("Example: java -cp \".;bin\" muse2.SimpleBatchProcessor input_wavs results.csv");
            return;
        }
        
        String inputDir = args[0];
        String outputCsv = args[1];
        
        try {
            processDirectory(inputDir, outputCsv);
            System.out.println("Batch processing complete! Results saved to: " + outputCsv);
        } catch (Exception e) {
            System.err.println("Error during batch processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void processDirectory(String inputDir, String outputCsv) throws IOException {
        File directory = new File(inputDir);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Input directory does not exist: " + inputDir);
        }
        
        // Get all WAV files
        File[] wavFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (wavFiles == null || wavFiles.length == 0) {
            System.out.println("No WAV files found in directory: " + inputDir);
            return;
        }
        
        System.out.println("Processing " + wavFiles.length + " WAV files...");
        
        // Process with different frequency shifts
        float[] shiftAmounts = {0, 200, 400, 600}; // Hz
        
        try (muse2.CsvExporter exporter = new muse2.CsvExporter(outputCsv)) {
            exporter.writeHeader();
            
            for (File wavFile : wavFiles) {
                System.out.println("Processing: " + wavFile.getName());
                
                // Load original audio
                muse2.AudioProcessor processor = new muse2.AudioProcessor(wavFile.getPath());
                float[] leftChannel = processor.getLeftChannel();
                float[] rightChannel = processor.getRightChannel();
                float sampleRate = processor.getSampleRate();
                
                // Measure original spatial cues
                double originalITD = muse2.SpatialCueAnalyzer.computeITD(leftChannel, rightChannel, sampleRate);
                double originalILD = muse2.SpatialCueAnalyzer.computeILD(leftChannel, rightChannel);
                
                // Process with different shifts
                for (float shiftAmount : shiftAmounts) {
                    try {
                        // Apply frequency shifting
                        float[][] shifted = muse2.SimpleFrequencyShifter.shift(
                            leftChannel, rightChannel, sampleRate, shiftAmount);
                        
                        // Measure shifted spatial cues
                        double shiftedITD = muse2.SpatialCueAnalyzer.computeITD(shifted[0], shifted[1], sampleRate);
                        double shiftedILD = muse2.SpatialCueAnalyzer.computeILD(shifted[0], shifted[1]);
                        
                        // Calculate changes
                        double itdChange = shiftedITD - originalITD;
                        double ildChange = shiftedILD - originalILD;
                        
                        // Write to CSV
                        exporter.appendRow(
                            wavFile.getName(),
                            shiftAmount,
                            originalITD,
                            originalILD,
                            shiftedITD,
                            shiftedILD,
                            itdChange,
                            ildChange
                        );
                        
                        System.out.printf("  Shift %.0fHz: ITD %.1f→%.1fμs (%.1f), ILD %.1f→%.1fdB (%.1f)%n",
                            shiftAmount, originalITD, shiftedITD, itdChange, originalILD, shiftedILD, ildChange);
                        
                    } catch (Exception e) {
                        System.err.println("  Error processing shift " + shiftAmount + "Hz: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("Batch processing completed successfully!");
    }
} 