package cz.muni.fi.keycheck.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 03.11.2015
 */
public abstract class ExportContainer implements Stats {
    private BufferedWriter[] writers;
    private boolean anythingWritten = false;

    public ExportContainer() {
        String[] fileNames = getFilesName();
        writers = new BufferedWriter[fileNames.length];
        for (int i = 0; i < writers.length; i++) {
            String fileName = fileNames[i];
            try {
                writers[i] = new BufferedWriter(new FileWriter(fileName));
            } catch (IOException ex) {
                System.err.println("Cannot open file '" + fileName + "' for export output.");
                writers[i] = null;
            }
        }
    }

    protected abstract String[] getFilesName();

    protected abstract String[] getTransformed(Params params);

    public void changeCard(String icsn) {
        String[] fileNames = getFilesName();
        for (int i = 0; i < writers.length; i++) {
            String fileName = fileNames[i];
            BufferedWriter writer = writers[i];
            if (writer != null) {
                try {
                    if (anythingWritten) {
                        writer.newLine();
                    } else {
                        anythingWritten = true;
                    }
                    writer.write(icsn);
                } catch (IOException ex) {
                    System.err.println("Error on export. Cannot write to file " + fileName + ".");
                }
            }
        }
    }

    public void changeCard(String icsn, long numOfKeys) {
        changeCard(icsn);
    }

    public void process(Params params) {
        String[] fileNames = getFilesName();
        String[] data = getTransformed(params);
        for (int i = 0; i < fileNames.length; i++) {
            if (anythingWritten) {
                try {
                    writers[i].write(";" + data[i]);
                } catch (IOException ex) {
                    System.err.println("Error on export. Cannot write to file " + fileNames[i] + ".");
                }
            }
        }
    }

    public void print() {
        String[] fileNames = getFilesName();
        for (int i = 0; i < writers.length; i++) {
            String fileName = fileNames[i];
            BufferedWriter writer = writers[i];
            if (writer == null)
                continue;

            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                System.err.println("Cannot close file " + fileName + ".");
            }
        }
    }
}
