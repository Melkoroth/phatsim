/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phat.audio.listeners;

import phat.sensors.Sensor;
import phat.sensors.SensorData;
import phat.sensors.SensorListener;
import phat.sensors.microphone.MicrophoneData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * A listener class of MicrophoneSensor that sends the audio to the speakers.
 *
 * @TODO It is a really slow way to listen a scenario. Find other methods.
 * (OpenAL?)
 *
 * @author pablo
 */
public class PCSpeaker implements SensorListener {

    private boolean initialized = false;
    private SourceDataLine sourceDataLine;

    public void init(AudioFormat format) {
        DataLine.Info dataLineInfo =
                new DataLine.Info(
                SourceDataLine.class,
                format);

        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            System.out.println("EEEEEEEERRRRRRRRRROOOOOOOOORRRRR!!!!!!!!");
            return;
        }

        System.out.println("Format = " + dataLineInfo);
        try {
            for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
                System.out.println("Encodig = " + mi.getName());
                System.out.println("\t" + mi.getDescription());
                System.out.println("\t" + mi.getVendor());
                System.out.println("\t" + mi.getVersion());
            }
            sourceDataLine = getSourceDataLine(dataLineInfo);

            System.out.println("Open");
            sourceDataLine.open(format);
            System.out.println("Start sourceDataLine = " + sourceDataLine.getLineInfo());
            sourceDataLine.start();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PCSpeaker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void update(Sensor source, SensorData sd) {
        if (sd instanceof MicrophoneData) {
            MicrophoneData md = (MicrophoneData) sd;
            if (!initialized) {
                init(md.getAudioFormat());
                initialized = true;
            }
            sourceDataLine.flush();
            sourceDataLine.write(md.getData(), 0, md.getData().length);            
            /*
             int available = sourceDataLine.available();
             int length = md.getData().length;
             if(available < length) {
             sourceDataLine.write(md.getData(), 0, available);
             } else {
             sourceDataLine.write(md.getData(), 0, md.getData().length);
             }*/
        }
    }

    private SourceDataLine getSourceDataLine(DataLine.Info dataLineInfo) {
        SourceDataLine sdl = null;
        for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
            Mixer m = AudioSystem.getMixer(mi);
            if (m.isLineSupported(dataLineInfo)) {
                try {
                    Line line = m.getLine(dataLineInfo);
                    if (line instanceof SourceDataLine) {
                        Logger.getLogger(
                                PCSpeaker.class.getName()).log(Level.SEVERE, null, "Mixer=" + m.getMixerInfo().getName());
                        sdl = (SourceDataLine) line;
                    }
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(PCSpeaker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return sdl;
    }

    @Override
    public void cleanUp() {
        if (sourceDataLine != null) {
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
        }
    }
}