package edu.hebrew.db.merge;

import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/**
 * In this exercise we implemented a program which uses external merge sort to sort big files containing lines of chars.
 * It's assumed that every line in the file contains no more than 50 characters.
 * This program can perform with less than 50 MB of RAM available to it.
 * This is the class which implements it.
 */
public class ExternalMergeSort implements IMergeSort {
    private static final int BUFFER_SIZE = 1000000;
    private static final int LEAF_SIZE = 10000; // Number of lines in the initial temporary files
    private static final int MERGE_AMOUNT = 10; // Number of files to merge simultaneously
    private static final String READ_PREFIX = "r";
    private static final String WRITE_PREFIX = "w";
    private static final String TEMP_FILE_NAME = "leaf";
    private static final String ERROR_MESSAGE = "There was a problem with the input!";
    private static final String SUCCESS_MESSAGE = "Success!";
    private static final String TIME_MESSAGE = "Execution time is: ";
    private static final int MILLISECONDS_IN_SECOND = 1000;

    @Override
    public void sortFile(String in, String out, String tmpPath)
    {
        long startTime = System.currentTimeMillis();
        int numOfFiles = splitToSmallFiles(in, tmpPath);
        if (numOfFiles == -1)
        {
            System.err.println(ERROR_MESSAGE);
            return;
        }
        if(mergeFiles(numOfFiles, tmpPath, out))
        {
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println(SUCCESS_MESSAGE);
            System.out.println(TIME_MESSAGE);
            System.out.print(elapsedTime / MILLISECONDS_IN_SECOND);
            System.out.print(" seconds\n");
        }
        else
        {
            System.err.println(ERROR_MESSAGE);
        }
    }

    /**
     * Delete a file
     * @param out : path to the file to delete
     * @throws IOException : if a file is found and deletion fails
     */
    private void deleteDestinationFile(String out) throws IOException
    {
        File fileToDelete = new File(out);
        if (fileToDelete.exists())
        {
            if (!fileToDelete.delete())
            {
                throw new IOException();
            }
        }
    }

    /**
     * Split the original file into a lot of small files.
     * @param in : the path to the original file
     * @param tmpPath : directory for temporary processing files
     * @return : -1 if there was a problem, else returns the amount of small files created.
     */
    private int splitToSmallFiles(String in, String tmpPath)
    {
        try
        {
            FileReader fr = new FileReader(in);
            BufferedReader br = new BufferedReader(fr, BUFFER_SIZE);
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
            br.close();
            return fileIndex;
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    /**
     * Creates an empty file.
     * @param out : path to where put the empty file
     * @throws IOException : upon failure creating the file
     */
    private void createEmptyFile(String out) throws IOException
    {
        File emptyFile = new File(out);
        if (!emptyFile.createNewFile())
        {
            throw new IOException();
        }
    }

    /**
     * Sorts an array of string lexicographically using Java array sort algorithm and then writes it to file, every
     * member in the array is written in its own line in lexicographical order.
     * Assumptions on array: all the nulls are from the left and they are continuously placed.
     * @param leaf : the array to sort
     * @param i : index where nulls will start to appear in the array
     * @param fileIndex : the index of the name of the file
     * @param tmpPath : the directory where to store the file
     * @throws IOException : failure saving the files
     */
    private void smallFileSortAndSave(String[] leaf, int i, int fileIndex, String tmpPath) throws IOException
    {
        Arrays.sort(leaf, 0, i);
        String fileName = TEMP_FILE_NAME + String.valueOf(fileIndex);
        FileWriter fw = new FileWriter(tmpPath + READ_PREFIX + fileName);
        BufferedWriter bw = new BufferedWriter(fw, BUFFER_SIZE);
        String delimiter = "";
        for (int j = 0; j < i; j++)
        {
            bw.write(delimiter + leaf[j]);
            delimiter = "\n";
        }
        bw.close();
    }

    /**
     * Merges the files in the temporary directory.
     * If there is already a file exists in the "out" directory - deletes it.
     * If the program was started with a path leading to an empty file, an empty file is being created in the "out"
     * path provided.
     * @param numOfFiles : number of files to merge
     * @param tmpPath : path to the temporary directory
     * @param out : path where to save the sorted file
     * @return : true if the merged finished successfully.
     */
    private boolean mergeFiles(int numOfFiles, String tmpPath, String out)
    {
        try
        {
            deleteDestinationFile(out);
            while(numOfFiles >= 1)
            {
                int n = (int)Math.ceil((float)numOfFiles / (MERGE_AMOUNT));
                for (int i = 0; i < n; i++)
                {
                    int firstFileIndex = i * MERGE_AMOUNT;
                    int lastFileIndex = firstFileIndex + MERGE_AMOUNT - 1;
                    int numOfFilesToMerge = Math.min(numOfFiles - 1, lastFileIndex) - firstFileIndex + 1;
                    BufferedReader[] fileReadBuffers = getBuffers(firstFileIndex, numOfFilesToMerge, tmpPath);
                    if (numOfFiles <= MERGE_AMOUNT)
                    {
                        mergeCurrentFiles(fileReadBuffers, numOfFilesToMerge, out, i, true);
                    }
                    else
                    {
                        mergeCurrentFiles(fileReadBuffers, numOfFilesToMerge, tmpPath, i, false);
                    }
                }
                if (numOfFiles > MERGE_AMOUNT)
                {
                    prepareTmpForNewRound(numOfFiles, n, tmpPath, false);
                    numOfFiles = n;
                }
                else
                {
                    prepareTmpForNewRound(numOfFiles, n, tmpPath, true);
                    numOfFiles = 0;
                }
            }
            File createdFile = new File(out);
            if (!createdFile.exists())
            {
                createEmptyFile(out);
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Deletes the temporary files that were merged (have "READ_PREFIX") into new temporary files (have "WRITE_PREFIX"),
     * renames the reminded temporary files to be of "READ_PREFIX".
     * @param numOfFilesBeforeMerge : number of temporary files before the merge operation took place
     * @param numOfFilesAfterMerge : number of temporary files after the merge operation took place
     * @param tmpPath : path to the temporary directory
     * @param isLast : true if this is the last merge operation of the program, else should be false
     * @throws IOException : if problem in deletion or renaming of temporary files occurs
     */
    private void prepareTmpForNewRound(int numOfFilesBeforeMerge, int numOfFilesAfterMerge, String tmpPath,
                                       boolean isLast) throws IOException
    {
        for (int i = 0; i < numOfFilesBeforeMerge; i++)
        {
            File fileToDelete = new File(tmpPath + READ_PREFIX + TEMP_FILE_NAME + String.valueOf(i));
            if (!fileToDelete.delete())
            {
                throw new IOException();
            }
        }
        if(!isLast)
        {
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

    }

    /**
     * Initializes read buffers for the temporary files insed the temporary directory.
     * @param firstFileIndex : the index of the first file to whom a read buffer should be allocated
     * @param numOfFilesToMerge : number of files waiting for merge
     * @param tmpPath : the temporary directory
     * @return : array of read buffers for temporary files
     * @throws IOException : upon failure handling the buffers
     */
    private BufferedReader[] getBuffers(int firstFileIndex, int numOfFilesToMerge, String tmpPath)
            throws IOException
    {
        BufferedReader[] fileReadBuffers = new BufferedReader[numOfFilesToMerge];
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            String currentFileName = tmpPath + READ_PREFIX + TEMP_FILE_NAME
                    + String.valueOf(firstFileIndex + i);
            FileReader fr = new FileReader(currentFileName);
            BufferedReader br = new BufferedReader(fr, BUFFER_SIZE / (MERGE_AMOUNT + 1));
            fileReadBuffers[i] = br;
        }
        return fileReadBuffers;
    }

    /**
     * Merges current files
     * @param fileReadBuffers : array of read buffers allocated for the files to be merged now
     * @param numOfFilesToMerge : number of files to be merged
     * @param currentPath : path where to save the merged file
     * @param currentMerge : the index of merged file to be created
     * @param isLast : true if this is the last merge operation of the program, else should be false
     * @throws IOException : upon failure handling the buffers
     */
    private void mergeCurrentFiles(BufferedReader[] fileReadBuffers, int numOfFilesToMerge, String
            currentPath, int currentMerge, boolean isLast) throws IOException
    {
        String[] rows = new String[numOfFilesToMerge];
        int numOfRowsToFill = fillRows(rows, fileReadBuffers, numOfFilesToMerge);
        String mergedFilePath = currentPath + WRITE_PREFIX + TEMP_FILE_NAME + String.valueOf(currentMerge);
        if (isLast)
        {
            mergedFilePath = currentPath;
        }
        FileWriter fw = new FileWriter(mergedFilePath);
        BufferedWriter bw = new BufferedWriter(fw, BUFFER_SIZE / (MERGE_AMOUNT + 1));
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

    /**
     * Fills an array of string with first lines to be writted to a merged file from buffers.
     * @param rows : an array of strings
     * @param fileReadBuffers : an array of read buffers
     * @param numOfFilesToMerge : number of files to be merged
     * @return : number of buffers that still have lines to be filled into the merged file.
     * @throws IOException : upon failure handling the buffers
     */
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

    /**
     * Returns the index of the string which is first according to lexicographical order in string array
     * @param rows : string array
     * @param numOfFilesToMerge : number of files to be merged
     * @return :
     */
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

    /**
     * Closes all the read buffers in the array
     * @param fileReadBuffers : array of read buffers
     * @param numOfFilesToMerge : number of files to be merged
     * @throws IOException : upon failure handling the buffers
     */
    private void closeFileReadBuffers(BufferedReader[] fileReadBuffers, int numOfFilesToMerge)
            throws IOException
    {
        for (int i = 0; i < numOfFilesToMerge; i++)
        {
            fileReadBuffers[i].close();
        }
    }
}
