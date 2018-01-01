package edu.hebrew.db.merge;

public interface IMergeSort {
    /**
     * Sort `in` file in `out` file
     * @param in Filename to sort
     * @param out Filename of output
     * @param tmpPath path to store tmp files
     */
    void sortFile(String in, String out, String tmpPath);
}
