import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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
        }
    }


    private static void writeCodeTable(OutputBitStream output, String[] st) throws IOException {
        int cnt=0;
        for(int i=0;i<st.length;i++) {
            if(st[i]!=null) {
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

    public static void compress(String inputFile,String outputFile) throws IOException {
        byte input[]= Files.readAllBytes(new File(inputFile).toPath());
        int freq[]=computeFrequencies(input);

        HufmanNode root = buildHufmanTree(freq);

        // build code table
        String[] st = new String[R];
        buildCodeTable(st, root, "");

       try{
           OutputBitStream output=new OutputBitStream(new FileOutputStream(outputFile));
           writeCodeTable(output,st);
           output.writeInt(input.length);
           for(byte b : input){
               String bits=st[b & 0xff];
               for(char c : bits.toCharArray()){
                   output.writeBit(c=='1');
               }
           }

           output.close();
       }
       catch(IOException e){
           e.printStackTrace();
       }

    }

    private static Map <String, Byte> readCodeTable(InputBitStream input) throws IOException{
        int cnt=input.readInt();//no of different characters
        Map<String, Byte>code=new HashMap<>();

        for(int i=0;i<cnt;i++){
            byte ch= (byte) input.readByte();//character
            int len=input.readInt();//no of bits in code

            StringBuffer sb=new StringBuffer(len);
            for(int j=0;j<len;j++){
                boolean bit=input.readBit();
                sb.append(bit? '1':'0');
            }
            code.put(sb.toString(),ch);
        }
        return code;
    }

    public static void decompress(String inputFile,String outputFile) throws IOException {
        try{
            InputBitStream input=new InputBitStream(new FileInputStream(inputFile));
            FileOutputStream output=new FileOutputStream(outputFile);

            Map <String, Byte> code=readCodeTable(input);
            int dataLen=input.readInt();
            int bytesWritten=0;

            StringBuffer sb=new StringBuffer();
            while(bytesWritten<dataLen){
                while(true) {
                    sb.append(input.readBit() ? '1' : '0');
                    if (code.containsKey(sb.toString())) {
                        output.write(code.get(sb.toString()));
                        bytesWritten++;
                        sb.setLength(0);
                        break;
                    }
                }
            }
            output.close();

        } catch (IOException e){
            e.printStackTrace();
        }
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
                System.out.println("Compression successful");
            }
            else if(mode.equals("-d")){
                decompress(inputFile,outputFile);
                System.out.println("Decompression successful");
            }
            else System.out.println("Error. Unrecognized mode. -c -> compression; -d -> decompression");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}