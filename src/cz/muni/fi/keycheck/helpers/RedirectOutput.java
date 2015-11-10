package cz.muni.fi.keycheck.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 07.11.2015
 */
public class RedirectOutput {
    private static PrintStream console = null;
    private static LineModifier lineModifier = null;

    /**
     * Redirect standard output to file
     * @param fileName file as new standard output
     */
    protected static void redirectOutputToFile(String fileName) {
        console = System.out;
        lineModifier = new LineModifier(console);
        try {
            System.setOut(new PrintStream(new FileOutputStream(fileName)));
        } catch (SecurityException ex) {
            System.err.println("ERROR:");
            System.err.println("  Cannot redirect output stream to " + fileName + ".");
            System.err.println("  You do not have write access on file " + fileName + ".");
            System.err.println("  All information will be written to the console.\n");
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR:");
            System.err.println("  Cannot redirect output stream to file " + fileName + ".");
            System.err.println("  File " + fileName + " is not found or is not correct file.");
            System.err.println("  All information will be written to the console.\n");
        }
    }

    /**
     * Get console if is output redirected
     * @return console|standard output if is not redirected
     */
    protected static PrintStream getConsole() {
        if (console == null) {
            return System.out;
        }
        else {
            return console;
        }
    }

    /**
     * Use line modifier on console if is standard output redirected
     * @see cz.muni.fi.keycheck.helpers.LineModifier
     */
    protected static void consoleDoneLine() {
        if (lineModifier == null) {
            getConsole().println();
        }
        else {
            lineModifier.doneLine();
        }
    }

    /**
     * Use line modifier on console if is standard output redirected
     * @param newLine new line content
     * @see cz.muni.fi.keycheck.helpers.LineModifier
     */
    protected static void consoleReplaceLine(String newLine) {
        if (lineModifier == null) {
            getConsole().print("\n" + newLine);
        }
        else {
            lineModifier.replaceLine(newLine);
        }
    }
}
