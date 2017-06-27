package com.eldar.srm;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A dictionary that owns all the dictionary entries and controls their relationships and order.
 * Created by eldarm on 6/2/17.
 */

class Dictionary {

    // dict is never null and we always have at least one, maybe empty, internal ArrayList.
    private ArrayList<ArrayList<Entry>> dict;
    private int count = 0;
    private final int numOfLang = 3; // TODO: Fix and get from the first line.
    private final int numOfLists = 3; // We expected it to be at least 3, or it will break.

    private final String loadTag = "TASK: Loading: ";
    private final String saveTag = "TASK: Saving: ";
    private final String mergeTag = "TASK: Merging: ";
    private final String workTag = "TASK: Testing: ";
    private final String maintainTag = "TASK: Maintain: ";

    Dictionary() {
        clear();
    }

    Dictionary(BufferedReader reader) {
        read(reader);
    }

    private void clear() {
        dict = new ArrayList<ArrayList<Entry>>();
        for (int i=0; i<numOfLists; ++i) {
            dict.add(new ArrayList<Entry>());
        }
        count = 0;
    }

//    private void add(int index, Entry e) {
//        dict.get(index).add(e);
//    }

    private Entry makeFake() {
        return Entry.parse("\tNo words found\tPlease load / merge a dictionary", 2);
    }

    private void normalize() {
        for (int i = 1; dict.get(0).size() == 0 && i < numOfLists; ++i) {
            Log.d(maintainTag, "Normalize the list");
            dict.add(dict.remove(0)); // TODO: Is it safe?
        }
    }

    /**
     * Merges a dictionary into the current one.
     *
     * @param newDict: Dictionary to merge into the current one.
     *               It is assumed that the newDict is throwable after the merge.
     * @return this for the cascade pattern.
     */
    Dictionary merge(Dictionary newDict) {
        Log.d(mergeTag,
              String.format("Merging %d lists into %d lists.", newDict.dict.size(), dict.size()));
        for (ArrayList<Entry> src : newDict.dict) {
            // Add new entries in a random order since newDict is likely to be pre-sorted.
            Collections.shuffle(src);
            for (Entry e : src) {
                boolean done = false;
                for (ArrayList<Entry> dst : dict) {
                    for (int j = 0; j < dst.size(); j++) {
                        if (dst.get(j) == e) {
                            dst.set(j, e);
                            Log.d(mergeTag, String.format("Found %s in the list #%d.", e.getWord(), j));
                            done = true;
                            break;
                        }
                    }
                    if (done) break;
                }
                if (!done) {
                    dict.get(0).add(e);
                    Log.d(mergeTag, String.format("Adding %s to the list #0.", e.getWord()));
                }
            }
        }
        return this;
    }

    /**
     * Reads a dictionary from the reader into the current one. Replaces the current content.
     *
     * @param reader: java.io.Reader with the string representation of a DIctionary to read.
     * @return this for the cascade pattern.
     */
    Dictionary read(BufferedReader reader) {
        clear();
        String line = "";
        try {
            if (dict.size() != numOfLists) throw new AssertionError(); // TODO: Remove later. ******
            int curList = 0;
            ArrayList<Entry> current = dict.get(0);
            int wordsCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (wordsCount == 0 && line.startsWith("Category\t")) {
                    // Version 0.1 Beta format header line, skip.
                    continue;
                }
                if (line.charAt(0) == '#') {
                    if (line.startsWith("# list ")) {
                    Log.d(loadTag, String.format("Uploaded %d words in a list.", current.size()));
                    // Preprocessor directives, right now we only have list.
                    if (curList + 1 < dict.size()) {
                        ++curList;
                    }
                    current = dict.get(curList);
                    } else {
                        // Something else, may be the header row.
                        // Do nothing, just discard.
                        // TODO: Add header '#' lines, which will change the number of languages.
                    }
                    continue;
                }
                Entry e = Entry.parse(line, numOfLang);
                if (e != null) {
                    current.add(e);
                    ++wordsCount;
                }
            }
            normalize();
            Log.d(loadTag, String.format("Uploaded %d words in a list.", current.size()));
            Log.d(loadTag, String.format("Uploaded %d lists.", dict.size()));
            for (int i=0; i<dict.size(); ++i) {
                Log.d(loadTag, String.format("List #%d has %d elements.", i, dict.get(i).size()));
            }
        } catch (UnsupportedCharsetException e) {
            Log.e(loadTag, "Charset string is incorrect or not recognized: " + e.toString());
        } catch (IOException e) {
            Log.e(loadTag, e.toString());
        }

        return this;
    }

    /**
     * Write the dictionary to a writer. Does not change the state of th dictionary.
     *
     * @param writer: java.io.Writer where to write the serialized dictionary.
     * @return this for the cascade pattern.
     */
    Dictionary write(BufferedWriter writer) {
        try {
            for (int i = 0; i < dict.size(); i++) {
                writer.write(String.format("# list %d of %d", i, dict.size()));
                writer.newLine();
                for (int j = 0; j < dict.get(i).size(); j++) {
                    writer.write(dict.get(i).get(j).toString());
                    writer.newLine();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(saveTag, "Cannot make new file: " + e.toString());
        } catch (IOException e) {
            Log.e(saveTag, "Cannot save file: " + e.toString());
        }

        return this;
    }

    // Methods for handling entries turnover.

    // Attention: we currently have only 3 list,
    // if we'll ever have 7 or more, need to handle that properly.
    private final int[] spacing = {5, 12, 27, 71, 100};
    private int index = 0;

    /**
     * Pich the next entry to show.
     *
     * @return Entry to show.
     */
    Entry next() {
        if (dict.get(0).size() == 0) {
            Log.d(workTag, String.format("Number of lists: %d, list #0 size %d.",
                                         dict.size(), dict.get(0).size()));
            return makeFake();
        }
        int index = 0;
        count++;

        Log.d(workTag, String.format("Number of lists: %d.", dict.size()));

        for (int i = 1; i < dict.size(); ++i) {
            if (count % spacing[i] == 0 && dict.get(i).size() > 0) {
                index = i;
                // No break, we pick the highest one.
            }
        }
        Log.d(workTag, String.format("Picked the list %d.", index));
        return dict.get(index).get(0);
    }

    /**
     * Bad, failed guess. Push the entry upfront for repetition.
     */
    void front() {
        Log.d(workTag, "Move to the front.");
        move(0);
    }

    /**
     * Push the entry back a bit.
     */
    void keep() {
        Log.d(workTag, "Keep in place.");
        move(index + 1 == dict.size() ? index : index + 1);
    }

    /**
     * Well done! Push the known entry to the back.
     */
    void back() {
        Log.d(workTag, "Move to the back.");
        move(dict.size() - 1);
    }

    private void move(int newIndex) {
        if (dict.get(0).size() == 0) {
            // Nothing to do.
            return;
        }
        dict.get(newIndex).add(dict.get(index).remove(0));
        if (dict.get(0).size() == 0) {
            // It's a victory, everything in the front list was learned well. Shift.
            normalize();
        }
    }
}
