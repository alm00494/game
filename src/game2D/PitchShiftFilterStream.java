package game2D;

import java.io.*;

/**
 * PitchShiftFilterStream is a custom FilterInputStream that attempts to apply
 * pitch-shifting to an audio stream. This implementation resamples the audio
 * data at a different rate determined by the pitchShiftFactor. However, the
 * resulting audio has a static-like noise - further research leads me to feel like
 * the approach used is too basic and introduces artifacts in the audio.
 * <p>
 * This class was created to experiment with pitch-shifting but could not achieve
 * the desired outcome.
 *
 * @author 2925642
 */
public class PitchShiftFilterStream extends FilterInputStream {

    private float pitchShiftFactor;

    // Constructor takes an InputStream and a pitchShiftFactor
    public PitchShiftFilterStream(InputStream in, float pitchShiftFactor) {
        super(in);
        this.pitchShiftFactor = pitchShiftFactor;
    }

    @Override
    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);

        if (bytesRead == -1) {
            return -1;
        }

        // Create a new array for the pitch-shifted samples
        byte[] shiftedSamples = new byte[bytesRead];

        // resample the original audio data at a different rate determined by the pitchShiftFactor.
        // i think it's too basic and introduces artifacts in the audio as the resulting sound is staticy
        for (int i = 0; i < bytesRead / 2; i++) {
            int newIndex = Math.round(i * pitchShiftFactor) * 2;
            if (newIndex + 1 < bytesRead) {
                shiftedSamples[newIndex] = sample[i * 2];
                shiftedSamples[newIndex + 1] = sample[i * 2 + 1];
            }
        }

        // Copy the pitch-shifted samples back into the original sample array
        System.arraycopy(shiftedSamples, 0, sample, 0, bytesRead);

        return bytesRead;
    }
}

