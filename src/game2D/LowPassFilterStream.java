package game2D;

import java.io.*;

/**
 * LowPassFilterStream applies a low-pass filter to an audio stream.
 * This filter smooths out the audio data by averaging the samples within a specified filter size.
 *
 * @author 2925642
 */
public class LowPassFilterStream extends FilterInputStream {
    private int filterSize;
    private short[] buffer;
    private int bufferPos;


    public LowPassFilterStream(InputStream in, int filterSize) {
        super(in);
        this.filterSize = filterSize;
        buffer = new short[filterSize];
        bufferPos = 0;
    }

    @Override
    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);

        if (bytesRead == -1) {
            return -1;
        }

        for (int i = 0; i < bytesRead; i += 2) {
            short sampleValue = (short) (((sample[i + 1] & 0xff) << 8) | (sample[i] & 0xff));
            buffer[bufferPos] = sampleValue;
            bufferPos = (bufferPos + 1) % filterSize;

            short filteredValue = 0;
            for (int j = 0; j < filterSize; j++) {
                filteredValue += buffer[j];
            }
            filteredValue /= filterSize;

            sample[i] = (byte) (filteredValue & 0xFF);
            sample[i + 1] = (byte) ((filteredValue >> 8) & 0xFF);
        }

        return bytesRead;
    }
}
