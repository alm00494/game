package game2D;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EchoFilterStream is a custom FilterInputStream that applies an echo effect
 * to an audio stream. It works by delaying the audio data and mixing it with the
 * original data at a specified decay factor.
 *
 * @author 2925642
 */
public class EchoFilterStream extends FilterInputStream {

    private int delay;                // The delay of the echo effect in bytes
    private float decay;              // The decay factor of the echo effect
    private byte[] delayBuffer;       // The buffer to store delayed audio data
    private int delayBufferPos;       // The current position in the delay buffer

    /**
     * Constructs a new EchoFilterStream with the specified InputStream, delay, and decay.
     *
     * @param in    The InputStream to read audio data from
     * @param delay The delay of the echo effect in bytes
     * @param decay The decay factor of the echo effect
     */
    public EchoFilterStream(InputStream in, int delay, float decay) {
        super(in);
        this.delay = delay;
        this.decay = decay;
        delayBuffer = new byte[delay];
    }

    /**
     * Reads audio data from the InputStream, applies the echo effect, and writes
     * the result to the specified byte array.
     *
     * @param sample The byte array to store the processed audio data
     * @param offset The starting position in the array to write the data
     * @param length The maximum number of bytes to read
     * @return The total number of bytes read, or -1 if the end of the stream is reached
     * @throws IOException If an I/O error occurs
     */
    @Override
    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);

        if (bytesRead == -1) {
            return -1;
        }

        for (int i = 0; i < bytesRead; i++) {
            byte echoSample = delayBuffer[delayBufferPos];
            delayBuffer[delayBufferPos] = (byte) (sample[i] * decay);
            sample[i] = (byte) (sample[i] + echoSample);

            delayBufferPos++;
            if (delayBufferPos >= delay) {
                delayBufferPos = 0;
            }
        }

        return bytesRead;
    }
}
