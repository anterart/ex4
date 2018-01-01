package edu.hebrew.db.merge;

import java.io.*;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExternalMergeSort implements IMergeSort {
    private static final int LEAF_SIZE = 500;

    @Override
    public void sortFile(String in, String out, String tmpPath)
    {
        splitToSmallFiles(in, tmpPath);
    }

    private void splitToSmallFiles(String in, String tmpPath)
    {
        try
        {
            FileReader fr = new FileReader(in);
            BufferedReader br = new BufferedReader(fr);
            String[] leaf = new String[LEAF_SIZE];
            int i = 0;
            int fileIndex = 0;
            leaf[0] = br.readLine();
            String nextLine = br.readLine();
            while (leaf[i] != null || nextLine != null)
            {
                i++;
                if (i == LEAF_SIZE || nextLine == null)
                {
                    smallFileSortAndSave(leaf, i, fileIndex, tmpPath);
                    i = 0;
                    fileIndex++;
                }
                leaf[i] = nextLine;
                nextLine = br.readLine();
            }
            fr.close();
            leaf = null;
        }
        catch(Exception e)
        {

        }
    }

    private void smallFileSortAndSave(String[] leaf, int i, int fileIndex, String tmpPath) throws IOException
    {
        Arrays.sort(leaf, 0, i);
        String fileName = "leaf" + String.valueOf(fileIndex);
        FileWriter fw = new FileWriter(tmpPath + fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        String delimiter = "";
        for (int j = 0; j < i; j++)
        {
            bw.write(delimiter + leaf[j]);
            delimiter = "\n";
        }
        bw.close();
    }
}
