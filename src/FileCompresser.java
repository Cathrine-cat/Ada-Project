import java.io.IOException;

public class FileCompresser {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java FileCompresser <-c|-d> <input file> <output file>");
            return;
        }

        String mode = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        if(mode.equals("-c")){
            System.out.println("C");
        }
        else if(mode.equals("-d")){
            System.out.println("D");
        }
        else System.out.println("Error. Unrecognized mode. -c -> compression; -d -> decompression");
    }
}