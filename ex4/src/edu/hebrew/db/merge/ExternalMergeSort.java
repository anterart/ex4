package edu.hebrew.db.merge;

import java.io.*;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExternalMergeSort implements IMergeSort {
    private static final int LEAF_SIZE = 500;
    private static final int MERGE_AMOUNT = 10;
    private static final String READ_PREFIX = "r";
    private static final String WRITE_PREFIX = "w";
    private static final String TEMP_FILE_NAME = "leaf";

    @Override
    public void sortFile(String in, String out, String tmpPath)
    {
        int numOfFiles = splitToSmallFiles(in, tmpPath);
        mergeFiles(numOfFiles, tmpPath);
        moveSortedFile(tmpPath, out);
    }

    private void moveSortedFile(String tmpPath, String out)
    {
        try
        {
            File sortedFile = new File(tmpPath + READ_PREFIX + TEMP_FILE_NAME + "0");
            File outputFile = new File(out);
            if (outputFile.exists())
            {
                if(!outputFile.delete())
                {
                    throw new IOException();
                }
            }
            if(sortedFile.renameTo(outputFile))
            {
                System.out.println("success!");
            }
            else
            {
                System.out.println("failure!");
            }
        }
        catch (Exception e)
        {
            System.out.println("There was a problem!");
        }

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
        String fileName = TEMP_FILE_NAME + String.valueOf(fileIndex);
        FileWriter fw = new FileWriter(tmpPath + READ_PREFIX + fileName);
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
                int n = (int)Math.ceil((float)numOfFiles / (MERGE_AMOUNT));
                for (int i = 0; i < n; i++)
                {
                    int firstFileIndex = i * MERGE_AMOUNT;
                    int lastFileIndex = firstFileIndex + MERGE_AMOUNT - 1;
                    int numOfFilesToMerge = Math.min(numOfFiles - 1, lastFileIndex) - firstFileIndex + 1;
                    BufferedReader[] fileReadBuffers = getBuffers(firstFileIndex, numOfFilesToMerge, tmpPath);
                    mergeCurrentFiles(fileReadBuffers, numOfFilesToMerge, tmpPath, i);
                }
                prepareTmpForNewRound(numOfFiles, n, tmpPath);
                numOfFiles = n;
            }
            catch (Exception e)
            {
                System.out.println("There was a problem!");
            }
        }
    }

    private void prepareTmpForNewRound(int numOfFilesBeforeMerge, int numOfFilesAfterMerge, String tmpPath)
    throws IOException
    {
        for (int i = 0; i < numOfFilesBeforeMerge; i++)
        {
            File fileToDelete = new File(tmpPath + READ_PREFIX + TEMP_FILE_NAME + String.valueOf(i));
            if (!fileToDelete.delete())
            {
                throw new IOException();
            }
        }
        for (int i = 0; i < numOfFilesAfterMerge; i++)
        {
            File fileToRename = new File(tmpPath + WRITE_PREFIX + TEMP_FILE_NAME
                    + String.valueOf(i));
            File fileToRenameTo = new File (tmpPath + READ_PREFIX + TEMP_FILE_NAME
                    + String.valueOf(i));
            if(!fileToRename.renameTo(fileToRenameTo))
            {
                throw new IOException();
            }
        }
    }

    private BufferedReader[] getBuffers(int firstFileIndex, int numOfFilesToMerge, String tmpPath)
            throws IOException
    {
        BufferedReader[] fileReadBuffers = new BufferedReader[numOfFilesToMerge];
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            String currentFileName = tmpPath + READ_PREFIX + TEMP_FILE_NAME
                    + String.valueOf(firstFileIndex + i);
            FileReader fr = new FileReader(currentFileName);
            BufferedReader br = new BufferedReader(fr);
            fileReadBuffers[i] = br;
        }
        return fileReadBuffers;
    }

    private void mergeCurrentFiles(BufferedReader[] fileReadBuffers, int numOfFilesToMerge, String
            tmpPath, int currentMerge) throws IOException
    {
        String[] rows = new String[numOfFilesToMerge];
        int numOfRowsToFill = fillRows(rows, fileReadBuffers, numOfFilesToMerge);
        String mergedFilePath = tmpPath + WRITE_PREFIX + TEMP_FILE_NAME + String.valueOf(currentMerge);
        FileWriter fw = new FileWriter(mergedFilePath);
        BufferedWriter bw = new BufferedWriter(fw);
        String delimiter = "";
        while(numOfRowsToFill > 0)
        {
            int rowIndexToFill = findRowIndexToFill(rows, numOfFilesToMerge);
            String rowToFill = delimiter + rows[rowIndexToFill];
            rows[rowIndexToFill] = fileReadBuffers[rowIndexToFill].readLine();
            if (rows[rowIndexToFill] == null)
            {
                numOfRowsToFill--;
            }
            bw.write(rowToFill);
            delimiter = "\n";
        }
        bw.close();
        closeFileReadBuffers(fileReadBuffers, numOfFilesToMerge);
    }

    private int fillRows(String[] rows, BufferedReader[] fileReadBuffers, int numOfFilesToMerge)
            throws IOException
    {
        int numOfRowsToFill = 0;
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            rows[i] = fileReadBuffers[i].readLine();
            if (rows[i] != null)
            {
                numOfRowsToFill++;
            }
        }
        return numOfRowsToFill;
    }

    private int findRowIndexToFill(String[] rows, int numOfFilesToMerge)
    {
        String king = null;
        int indexKing = -1;
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            if (rows[i] != null)
            {
                king = rows[i];
                indexKing = i;
                break;
            }
        }
        if (indexKing == -1)
        {
            return -1;
        }
        for (int i = indexKing + 1; i < numOfFilesToMerge; i++)
        {
            if (rows[i] != null)
            {
                if (king.compareTo(rows[i]) >= 0)
                {
                    king = rows[i];
                    indexKing = i;
                }
            }
        }
        return indexKing;
    }

    private void closeFileReadBuffers(BufferedReader[] fileReadBuffers, int numOfFilesToMerge)
            throws IOException
    {
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            fileReadBuffers[i].close();
        }
    }
}
