package com.eldar.srm;

import android.util.Log;

/**
 * Class representing a single dictionary entry, including the category, word, translations
 * and a comment/example. Serializes/deserializes into/from a tab-separate line.
 * Created by eldar on 4/26/2017.
 */
class Entry {
    private String category;
    private String comment;
    private String[] words;

    private final String tagParsing = "Tag: PARSING: ";

    private Entry() {
    }

    static Entry parse(String line, int numOfLangs) {
        return new Entry().parsePrivate(line, numOfLangs);
    }

    private Entry parsePrivate(String line, int numOfLangs) {
        if (line == null || line.length() == 0) {
            Log.e(tagParsing, "Ignore an empty line.");
            return null;
        }
        Log.d(tagParsing, line);
        String[] parts = line.split("\t", 0);
        if (parts.length < 3) {
            // MUST have category (maybe empty), target word and at elast one translation.
            Log.e(tagParsing, String.format("Line '%s' parsed into %d parts, need at least 3",
                    line, parts.length));
            return null;
        }
        category = parts[0];
        words = new String[numOfLangs];
        int count = parts.length - 1;

        for (int i = 0; i < numOfLangs; i++) {
            words[i] = i < count ? parts[i + 1] : "";
        }
        comment = count > numOfLangs ? parts[count] : "";
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Entry)) {
            return false;
        }
        Entry other = (Entry) obj;
        return words[0].equals(other.words[0]);
    }

    @Override
    public int hashCode() {
        return words[0].hashCode();
    }

    String getCategory() {
        return category;
    }

    String getComment() {
        return comment;
    }

    String getWord() {
        return words.length > 0 ? words[0] : "";
    }

    String getTranslation(String sep) {
        // return translation;
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < words.length; i++) {
            sb.append(words[i] + sep);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        // return translation;
        StringBuffer sb = new StringBuffer();
        final String sep = "\t";
        // The source may be incomplete but we always save in a good format with all entries.
        sb.append(getCategory());
        sb.append(sep);
        sb.append(getWord());
        sb.append(sep);
        sb.append(getTranslation(sep));
        sb.append(sep);
        sb.append(getComment());
        return sb.toString();
    }
}
