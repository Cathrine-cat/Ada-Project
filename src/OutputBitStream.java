import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class OutputBitStream implements Closeable {
    private final OutputStream output;
    private int currByte;
    private int numBitsFiled;

    public OutputBitStream(OutputStream output) {
        this.output = output;
    }

    public void writeBit(boolean bit) throws IOException {
        currByte = (currByte << 1);
        if(bit)currByte=currByte|1;
        numBitsFiled++;
        if (numBitsFiled == 8) {
            flush();
        }
    }

    private void flush() throws IOException {
        if(numBitsFiled > 0) {
            currByte = currByte << (8 - numBitsFiled);
            output.write(currByte);
            numBitsFiled = 0;
        }
    }

    public void writeByte(byte b) throws IOException {
        if(numBitsFiled == 0) {
            output.write(b);
        }
        else {
            for(int i=7; i >= 0; i--) {
                writeBit(((b >> i) & 1)==1);
            }
        }
    }

    public void writeInt(int i) throws IOException {
        writeByte((byte)(i>>>24));
        writeByte((byte)(i>>>16));
        writeByte((byte)(i>>>8));
        writeByte((byte)i);
    }

    public void close() throws IOException {
        flush();
        output.close();
    }
}
