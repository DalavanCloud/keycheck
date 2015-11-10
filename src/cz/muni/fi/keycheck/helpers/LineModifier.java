package cz.muni.fi.keycheck.helpers;

import java.io.PrintStream;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 28.10.2015
 */
public class LineModifier {
    private PrintStream stream;
    private int lineWidth;

    /**
     * Create line modifier on stream
     * @param stream modifiable stream
     */
    LineModifier(PrintStream stream) {
        this(stream, 80);
    }

    /**
     * Create line modifier on stream with specified line width
     * @param stream modifiable stream
     * @param lineWidth line width
     */
    LineModifier(PrintStream stream, int lineWidth) {
        this.stream = stream;
        this.lineWidth = lineWidth;
    }

    /**
     * Replace last line on stream output with text
     * @param text new line content
     */
    public void replaceLine(String text) {
        if (text.length() >= lineWidth) {
            text = text.substring(0, lineWidth - 6);
            text += " ...";
        }
        stream.print('\r' + text);
        for (int i = text.length(); i < lineWidth - 1; i++) {
            stream.print(' ');
        }
    }

    /**
     * Set last line as not replaceable
     */
    public void doneLine() {
        stream.println();
    }
}
