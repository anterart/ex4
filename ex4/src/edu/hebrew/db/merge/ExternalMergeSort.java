package edu.hebrew.db.merge;

import java.io.*;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExternalMergeSort implements IMergeSort {
    private static final int LEAF_SIZE = 500;
    private static final int MERGE_AMOUNT = 2;
    private static final String PREFIX_1 = "a";
    private static final String PREFIX_2 = "b";
    private String prefix = PREFIX_1;

    @Override
    public void sortFile(String in, String out, String tmpPath)
    {
        int numOfFiles = splitToSmallFiles(in, tmpPath);
    }

    private int splitToSmallFiles(String in, String tmpPath)
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
            return fileIndex;
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private void smallFileSortAndSave(String[] leaf, int i, int fileIndex, String tmpPath) throws IOException
    {
        Arrays.sort(leaf, 0, i);
        String fileName = "leaf" + String.valueOf(fileIndex);
        FileWriter fw = new FileWriter(tmpPath + prefix + fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        String delimiter = "";
        for (int j = 0; j < i; j++)
        {
            bw.write(delimiter + leaf[j]);
            delimiter = "\n";
        }
        bw.close();
    }

    private void mergeFiles(int numOfFiles, String tmpPath)
    {
        while(numOfFiles > 1)
        {
            try
            {
                for (int i = 0; i < Math.ceil((float)numOfFiles / MERGE_AMOUNT); i++)
                {
                    int firstFileIndex = i * MERGE_AMOUNT;
                    int lastFileIndex = firstFileIndex + MERGE_AMOUNT - 1;
                    BufferedReader[] fileReadBuffers = getBuffers(firstFileIndex, lastFileIndex, tmpPath);
                    int numOfFilesToMerge = Math.min(numOfFiles, lastFileIndex) - firstFileIndex;
                    mergeCurrentBuffers(fileReadBuffers, numOfFilesToMerge, tmpPath);
                }
            }
            catch (Exception e)
            {

            }
        }
    }

    private BufferedReader[] getBuffers(int firstFileIndex, int lastFileIndex, String tmpPath)
            throws IOException
    {
        BufferedReader[] fileReadBuffers = new BufferedReader[MERGE_AMOUNT];
        for (int i = firstFileIndex; i <= lastFileIndex; i++)
        {
            FileReader fr = new FileReader(tmpPath + prefix + "leaf" + String.valueOf(i));
            BufferedReader br = new BufferedReader(fr);
            fileReadBuffers[i] = br;
        }
        changePrefix();
        return fileReadBuffers;
    }

    private void changePrefix()
    {
        if (prefix.equals(PREFIX_1))
        {
            prefix = PREFIX_2;
        }
        else
        {
            prefix = PREFIX_1;
        }
    }

    private void mergeCurrentBuffers(BufferedReader[] fileReadBuffers, int numOfFilesToMerge, String
            tmpPath) throws IOException
    {

    }




}
