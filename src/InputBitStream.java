import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputBitStream implements Cloneable{
    private final InputStream input;
    private int currByte;
    private int numBitsRemaining;

    public InputBitStream(InputStream input) {
        this.input = input;
        this.numBitsRemaining = 0;
    }

    public boolean readBit() throws IOException {
        if(numBitsRemaining == 0) {
            currByte = input.read();
            if(currByte == -1) throw  new EOFException();
            numBitsRemaining=8;
        }
        numBitsRemaining--;
        return ((currByte >> numBitsRemaining) & 1) ==1;
    }
}
