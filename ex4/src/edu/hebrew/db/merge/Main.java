package edu.hebrew.db.merge;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Not enough arguments");
            System.out.println("file : input_file output_file [tmp folder]");
            System.out.println("tmp folder is optional folder for temp files");
            System.exit(1);
        }

        String in = args[0];
        String out = args[1];

        String path = "./"; // current folder if nothing else is specified
        if (args.length >= 3) {
            path = args[2] + "/";
        }

        IMergeSort m = new ExternalMergeSort();
        m.sortFile(in, out, path);
    }
}
