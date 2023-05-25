package game2D;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FilterInputStream;

/**
 * The Sound class represents a sound effect in the game.
 * It is designed to play sound files using a separate thread for each sound instance.
 *
 * @author David Cairns
 * @author 2925642
 */
public class Sound extends Thread {

    String filename;    // The name of the file to play
    boolean finished;    // A flag showing that the thread has finished
    FilterInputStream filter; // An optional filter to apply to the sound


    /**
     * Constructs a Sound object with the specified file name.
     *
     * @param fname the name of the file to play
     */
    public Sound(String fname) {
        filename = fname;
        finished = false;
    }

    /**
     * Constructs a Sound object with the specified file name and FilterInputStream.
     *
     * @param fname  the name of the file to play
     * @param filter the FilterInputStream used to process the audio data
     */
    public Sound(String fname, FilterInputStream filter) {
        filename = fname;
        finished = false;
        this.filter = filter;
    }

    /**
     * run will play the actual sound but you should not call it directly.
     * You need to call the 'start' method of your sound object (inherited
     * from Thread, you do not need to declare your own). 'run' will
     * eventually be called by 'start' when it has been scheduled by
     * the process scheduler.
     */
    public void run() {
        try {
            File file = new File(filename);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            AudioInputStream filteredStream = stream;

            if (filter != null) {
                filteredStream = new AudioInputStream(filter, format, stream.getFrameLength());
            }

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(filteredStream);
            clip.start();
            Thread.sleep(100);
            while (clip.isRunning()) {
                Thread.sleep(100);
            }
            clip.close();
        } catch (Exception e) {
        }
        finished = true;
    }

    /**
     * Returns true if the sound has finished playing.
     * I thought I might need this
     *
     * @return true if the sound has finished playing
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Sets the finished flag to the specified value.
     * I thought I might need this
     *
     * @param finished the new value of the finished flag
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
