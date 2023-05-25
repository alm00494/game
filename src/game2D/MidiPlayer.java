package game2D;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.File;

/**
 * MidiPlayer is a utility class for playing MIDI files. It provides methods
 * to play, stop, and change MIDI files using the javax.sound.midi API.
 *
 * @author 2925642
 */
public class MidiPlayer {
    Sequencer sequencer;

    public void playMidi(String filename) {
        try {
            Sequence score = MidiSystem.getSequence(new File(filename));
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(score);
            sequencer.start();
        } catch (Exception e) {
        }
    }

    public void stopMidi() {
        if (sequencer != null) {
            sequencer.stop();
        }
    }

    public void changeMidi(String filename) {
        stopMidi();
        playMidi(filename);
    }

}
