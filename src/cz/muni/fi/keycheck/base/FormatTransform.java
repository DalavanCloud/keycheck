package cz.muni.fi.keycheck.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 03.11.2015
 */
public class FormatTransform implements Stats {
    private BufferedWriter writer = null;
    private long keyNumber = 0;

    public void changeCard(String icsn) {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                System.err.println("Cannot close file for export.");
            }
        }

        String fileName = icsn + ".transformed.csv";
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            keyNumber = 0;
        } catch (IOException ex) {
            System.err.println("Cannot open file '" + fileName + "' for transform output.");
            writer = null;
        }
    }

    public void changeCard(String icsn, long numOfKeys) {
        changeCard(icsn);
    }

    public void process(Params params) {
        if (writer != null) {
            try {
                params.writeToFile(writer, ++keyNumber);
            } catch (IOException ex) {
                System.err.println("Error on export. Cannot write to file.");
            }
        }
    }

    public void print() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                System.err.println("Cannot close file for export.");
            }
        }
    }
}
