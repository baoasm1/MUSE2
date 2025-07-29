package muse2;

import java.io.FileWriter;
import java.io.IOException;

public class CsvExporter implements AutoCloseable {
    private FileWriter writer;
    private boolean headerWritten = false;

    public CsvExporter(String filename) throws IOException {
        writer = new FileWriter(filename, false); // overwrite
    }

    public void writeHeader() throws IOException {
        if (!headerWritten) {
            writer.write("filename,shift_amount_hz,original_itd,original_ild,shifted_itd,shifted_ild,itd_change,ild_change\n");
            headerWritten = true;
        }
    }

    public void appendRow(String filename, float shiftAmountHz, double originalItd, double originalIld, double shiftedItd, double shiftedIld) throws IOException {
        double itdChange = shiftedItd - originalItd;
        double ildChange = shiftedIld - originalIld;
        writer.write(String.format("%s,%.1f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n",
                filename, shiftAmountHz, originalItd, originalIld, shiftedItd, shiftedIld, itdChange, ildChange));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
} 