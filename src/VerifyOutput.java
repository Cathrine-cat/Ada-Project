import java.io.FileInputStream;
import java.io.IOException;

public class VerifyOutput {
    public static void main(String[] args) {

        try {
            InputBitStream in = new InputBitStream(new FileInputStream(args[0]));
            while(true){
                try{
                    boolean bit=in.readBit();
                    System.out.print(bit ? "1" : "0");
                }
                catch (IOException e){//eof
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
