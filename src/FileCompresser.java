import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.PriorityQueue;

public class FileCompresser {

    private static int R = 256;

    private static class HufmanNode implements Comparable<HufmanNode> {
        char ch;
        int freq;
        HufmanNode left, right;

        HufmanNode(char ch, int freq, HufmanNode left, HufmanNode right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return (left == null) && (right == null);
        }


        public int compareTo(HufmanNode that) {
            return this.freq - that.freq;
        }
    }



    private static HufmanNode buildHufmanTree(int[] freq) {

        PriorityQueue<HufmanNode> pq = new PriorityQueue<>();
        for (char c = 0; c < R; c++)
            if (freq[c] > 0)
                pq.add(new HufmanNode(c, freq[c], null, null));

        while (pq.size() > 1) {
            HufmanNode left = pq.remove();
            HufmanNode right = pq.remove();
            HufmanNode parent = new HufmanNode('\0', left.freq + right.freq, left, right);
            pq.add(parent);
        }
        return pq.remove();
    }


    private static void buildCodeTable(String[] st, HufmanNode x, String s) {
        if (!x.isLeaf()) {
            buildCodeTable(st, x.left, s + '0');
            buildCodeTable(st, x.right, s + '1');
        } else {
            st[x.ch] = s;

            /**
             * Testing
             */
            System.out.println("Code of " + x.ch + " is " + s);
        }
    }


    public static int[] computeFrequencies(byte[] input){
        int[] freq = new int[R];
        for(byte b : input){
            freq[b & 0xff]++;
        }
        return freq;
    }

    public static void compress(String inputFile,String outputFile) throws IOException {
        byte input[]= Files.readAllBytes(new File(inputFile).toPath());
        int freq[]=computeFrequencies(input);


        /**
         * Testing
         */
        for (int i = 0; i < R; i++)
            if (freq[i] > 0)
                System.out.println("freq " + (char) i + " (" + (byte) i + ") " + freq[i]);

        HufmanNode root = buildHufmanTree(freq);

        // build code table
        String[] st = new String[R];
        buildCodeTable(st, root, "");


    }

    public static void decompress(String inputFile,String outputFile) throws IOException {

    }


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java FileCompresser <-c|-d> <input file> <output file>");
            return;
        }

        String mode = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        try{
            if(mode.equals("-c")){
                compress(inputFile,outputFile);
            }
            else if(mode.equals("-d")){
                decompress(inputFile,outputFile);
            }
            else System.out.println("Error. Unrecognized mode. -c -> compression; -d -> decompression");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}