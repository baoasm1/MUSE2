package muse2;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class BatchProcessor {
    private String[] shiftLabels = {"200", "400", "600"};
    private float[] shiftHz = {200f, 400f, 600f};

    public void processDirectory(String inputDir, String csvOut) {
        File dir = new File(inputDir);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".wav"));
        if (files == null) {
            System.err.println("No WAV files found in directory: " + inputDir);
            return;
        }
        // Create output directory for stimuli
        File outStimDir = new File("output_stimuli");
        if (!outStimDir.exists()) outStimDir.mkdir();
        try (CsvExporter exporter = new CsvExporter(csvOut)) {
            exporter.writeHeader();
            for (File file : files) {
                try {
                    System.out.println("Processing: " + file.getName());
                    AudioProcessor ap = new AudioProcessor(file.getAbsolutePath());
                    float[] left = ap.getLeftChannel();
                    float[] right = ap.getRightChannel();
                    float sr = ap.getSampleRate();
                    double origITD = SpatialCueAnalyzer.computeITD(left, right, sr);
                    double origILD = SpatialCueAnalyzer.computeILD(left, right);
                    for (int i = 0; i < shiftHz.length; i++) {
                        float shift = shiftHz[i];
                        float[][] shifted = FrequencyShifter.shift(left, right, sr, shift);
                        double shiftedITD = SpatialCueAnalyzer.computeITD(shifted[0], shifted[1], sr);
                        double shiftedILD = SpatialCueAnalyzer.computeILD(shifted[0], shifted[1]);
                        exporter.appendRow(file.getName(), shift, origITD, origILD, shiftedITD, shiftedILD);
                        System.out.printf("  Shift %s Hz: ITD %.2f us, ILD %.2f dB\n", shiftLabels[i], shiftedITD, shiftedILD);
                        // Save shifted audio as WAV
                        String outName = file.getName().replace(".wav", "_shifted_" + shiftLabels[i] + "Hz.wav");
                        File outFile = new File(outStimDir, outName);
                        writeStereoWav(shifted[0], shifted[1], sr, outFile);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing " + file.getName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }

    // Helper to write stereo float arrays to WAV file
    private void writeStereoWav(float[] left, float[] right, float sampleRate, File outFile) throws IOException {
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
                new java.io.ByteArrayInputStream(audioBytes), format, nFrames)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
        }
    }
} 