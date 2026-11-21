package com.automower.infrastructure.file;

import java.util.ArrayList;
import java.util.List;

public final class LineSplitter {

    private static final char LINE_FEED = '\n';
    private static final char CARRIAGE_RETURN = '\r';

    private LineSplitter() {
        // vide
    }

    public static List<String> split(String text) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        int lineFeed = text.indexOf(LINE_FEED);
        while (lineFeed >= 0) {
            lines.add(stripCarriageReturn(text.substring(start, lineFeed)));
            start = lineFeed + 1;
            lineFeed = text.indexOf(LINE_FEED, start);
        }
        // un unique saut de ligne final ne produit pas de ligne vide
        if (start < text.length()) {
            lines.add(text.substring(start));
        }
        return lines;
    }

    private static String stripCarriageReturn(String line) {
        return !line.isEmpty() && line.charAt(line.length() - 1) == CARRIAGE_RETURN ? line.substring(0, line.length() - 1) : line;
    }
}
