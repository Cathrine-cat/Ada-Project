import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class FileCompresser {

    private static final int R = 256;//size of alphabet
    private static final int CODE=0x48554646;//code used to identify files compressed by program

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
        }
    }


    private static void writeCodeTable(OutputBitStream output, String[] st) throws IOException {
        int cnt=0;
        for (String s : st) {
            if (s != null) {
                cnt++;
            }
        }

        //write the number of entries in the table
        output.writeInt(cnt);

        for(int i=0;i<st.length;i++) {
            if(st[i]!=null) {
                output.writeByte((byte)i);//write the symbol
                output.writeInt(st[i].length());//write lenght of code
                for(char bit:st[i].toCharArray()) {//write each bit of code
                    output.writeBit(bit=='1');
                }
            }

        }
    }

    public static int[] computeFrequencies(byte[] input){
        int[] freq = new int[R];
        for(byte b : input){
            freq[b & 0xff]++;
        }
        return freq;
    }

    public static int compress(String inputFile,String outputFile)  {

        byte[] input;
        try {
            input = Files.readAllBytes(new File(inputFile).toPath());
        }catch (IOException e){
            System.out.println("Error reading file");
            return 0;
        }
        int[] freq = computeFrequencies(input);

        HufmanNode root = buildHufmanTree(freq);

        // build code table
        String[] st = new String[R];
        buildCodeTable(st, root, "");

        /**
         * Form of compressed file:
         * Code:int, Code table, Lenght of input:int, Compressed code*/

        try (OutputBitStream output = new OutputBitStream(new FileOutputStream(outputFile))) {
            output.writeInt(CODE);//code that shows that file is compressed and can be decompreseed by this algorithm
            writeCodeTable(output, st);
            output.writeInt(input.length);
            for (byte b : input) {
                String bits = st[b & 0xff];
                for (char c : bits.toCharArray()) {
                    output.writeBit(c == '1');
                }
            }
        } catch(IOException e){
            System.out.println("Error while to writing file");
            return 0;
        }

        return 1;
    }

    private static Map <String, Byte> readCodeTable(InputBitStream input) throws IOException {
        int cnt=input.readInt();//no of different characters
        Map<String, Byte>code=new HashMap<>();

        for(int i=0;i<cnt;i++){
            byte ch= (byte) input.readByte();//character
            int len=input.readInt();//no of bits in code

            StringBuilder sb=new StringBuilder(len);
            for(int j=0;j<len;j++){
                boolean bit=input.readBit();
                sb.append(bit? '1':'0');
            }
            code.put(sb.toString(),ch);
        }
        return code;
    }

    public static int decompress(String inputFile,String outputFile) {
        try(InputBitStream input=new InputBitStream(new FileInputStream(inputFile))){
            int readcode=input.readInt();
            if(readcode!=CODE){//file was not compressed by this algorithm
                System.out.println("Invalid file format. Cannot be decompressed.");
                return 0;
            }

            Map <String, Byte> code=readCodeTable(input);
            int dataLen=input.readInt();
            int bytesWritten=0;

            try(FileOutputStream output=new FileOutputStream(outputFile)) {

                StringBuilder sb = new StringBuilder();
                while (bytesWritten < dataLen) {
                    while (true) {
                        sb.append(input.readBit() ? '1' : '0');
                        if (code.containsKey(sb.toString())) {
                            output.write(code.get(sb.toString()));
                            bytesWritten++;
                            sb.setLength(0);
                            break;
                        }
                    }
                }
            }catch(FileNotFoundException e) {
                System.out.println("Error opening output file");
                return 0;
            }catch(IOException e){
                System.out.println("Error while writing output file");
                return 0;
            }
        }catch (FileNotFoundException e) {
            System.out.println("Error opening input file");
            return 0;
        }catch (IOException e){
            System.out.println("Error while reading input file");
            return 0;
        }

        return 1;
    }


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java FileCompresser <-c|-d> <input file> <output file>");
            return;
        }

        String mode = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        if(mode.equals("-c")){
            if(compress(inputFile,outputFile)==1){
                System.out.println("Compression successful");
            }
            else System.out.println("Compression failed");
        }
        else if(mode.equals("-d")){
            if(decompress(inputFile,outputFile)==1){
                System.out.println("Decompression successful");
            }
            else System.out.println("Decompression failed");
        } else System.out.println("Error. Unrecognized mode. -c -> compression; -d -> decompression");
    }
}