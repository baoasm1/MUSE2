package muse2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String LOG_FILE = "muse2_processing.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static PrintWriter logWriter;
    private static boolean initialized = false;
    
    public static void initialize() {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true));
            logWriter.println("\n" + "=".repeat(80));
            logWriter.println("MUSE2 Processing Session Started: " + DATE_FORMAT.format(new Date()));
            logWriter.println("=".repeat(80));
            initialized = true;
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void warning(String message) {
        log("WARNING", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void error(String message, Exception e) {
        log("ERROR", message + ": " + e.getMessage());
        if (initialized && logWriter != null) {
            e.printStackTrace(logWriter);
        }
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    public static void logAudioFile(String filename, float sampleRate, int channels, long durationMs) {
        info(String.format("Processing audio file: %s (%.1f kHz, %d ch, %.1f s)", 
            filename, sampleRate/1000, channels, durationMs/1000.0));
    }
    
    public static void logSpatialCues(String filename, double itd, double ild) {
        info(String.format("Spatial cues for %s: ITD=%.1f μs, ILD=%.1f dB", filename, itd, ild));
    }
    
    public static void logFrequencyShift(String filename, float shiftHz, double itdChange, double ildChange) {
        info(String.format("Frequency shift %.0f Hz for %s: ITD change=%.1f μs, ILD change=%.1f dB", 
            shiftHz, filename, itdChange, ildChange));
    }
    
    public static void logProcessingTime(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        info(String.format("%s completed in %d ms", operation, duration));
    }
    
    private static void log(String level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] %s: %s", timestamp, level, message);
        
        // Console output
        System.out.println(logMessage);
        
        // File output
        if (initialized && logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
    
    public static void close() {
        if (initialized && logWriter != null) {
            info("MUSE2 Processing Session Ended");
            logWriter.close();
        }
    }
} 