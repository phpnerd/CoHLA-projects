package hla.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 29-8-16.
 */
public class DataCollector {

    private final int columns;
    private final String[] headers;
    private final List<double[]> data;

    private boolean enabled;

    public DataCollector(String... headers) {
        columns = headers.length;
        this.headers = headers;
        data = new ArrayList<>();
        enabled = true;
    }

    public void storeData(double... values) {
        data.add(values);
    }

    public boolean export(String filename) {
        enabled = false;
        File file = new File(filename);
        if (file.exists() && !file.delete()) {
            System.err.println("Failed to delete file");
            return false;
        } try {
            if (!file.createNewFile()) {
                System.err.println("Failed to create new file");
                return false;
            }
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            for (int i = 0; i < columns; i++)
                pw.write((i > 0 ? ";" : "") + "\"" + headers[i] + "\"");
            pw.write('\n');
            data.forEach(row -> {
                for(int i = 0; i < columns; i++)
                    pw.write((i > 0 ? ";" : "") + Double.toString(row[i]));
                pw.write('\n');
            });
            pw.flush();
            pw.close();
            return true;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    public int getColumns() {
        return columns;
    }

    public String[] getHeaders() {
        return headers;
    }

    public List<double[]> getData() {
        return data;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
