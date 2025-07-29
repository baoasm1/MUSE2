package muse2;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp <classpath> muse2.Main <input_wav_directory> <output_csv_file>");
            return;
        }
        String inputDir = args[0];
        String csvOut = args[1];
        BatchProcessor processor = new BatchProcessor();
        processor.processDirectory(inputDir, csvOut);
        System.out.println("Processing complete. Results written to " + csvOut);
    }
} 