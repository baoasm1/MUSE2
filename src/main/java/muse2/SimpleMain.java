package muse2;

public class SimpleMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp bin muse2.SimpleMain <input_wav_directory> <output_csv_file>");
            System.out.println("Example: java -cp bin muse2.SimpleMain input_wavs results.csv");
            return;
        }
        String inputDir = args[0];
        String csvOut = args[1];
        SimpleBatchProcessor processor = new SimpleBatchProcessor();
        processor.processDirectory(inputDir, csvOut);
        System.out.println("Processing complete. Results written to " + csvOut);
    }
} 