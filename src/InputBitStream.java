import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputBitStream implements Closeable {
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
            if(currByte == -1) throw new EOFException();
            numBitsRemaining=8;
        }
        numBitsRemaining--;
        return ((currByte >> numBitsRemaining) & 1) ==1;
    }

    public int readByte() throws IOException {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 1) | (readBit() ? 1 : 0);
        }
        return result;
    }

    public int readInt() throws IOException {
        return (readByte()<<24) | (readByte()<<16) | (readByte()<<8) | readByte();
    }

    public void close() throws IOException {
        input.close();
    }
}
