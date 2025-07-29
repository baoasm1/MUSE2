package muse2;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioProcessor {
    private float[] leftChannel;
    private float[] rightChannel;
    private float sampleRate;

    public AudioProcessor(String filename) throws Exception {
        loadWavFile(filename);
    }

    private void loadWavFile(String filename) throws Exception {
        File file = new File(filename);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();

        if (format.getChannels() != 2) {
            throw new Exception("Audio file must be stereo.");
        }
        if (format.getSampleRate() != 44100.0f) {
            throw new Exception("Audio file must be 44.1kHz.");
        }
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new Exception("Audio file must be PCM signed.");
        }

        this.sampleRate = format.getSampleRate();

        byte[] audioBytes = ais.readAllBytes();
        int frameSize = format.getFrameSize();
        int numFrames = audioBytes.length / frameSize;

        leftChannel = new float[numFrames];
        rightChannel = new float[numFrames];

        for (int i = 0; i < numFrames; i++) {
            int sampleIndex = i * frameSize;
            // 16-bit signed, little endian
            int left = (audioBytes[sampleIndex + 1] << 8) | (audioBytes[sampleIndex] & 0xFF);
            int right = (audioBytes[sampleIndex + 3] << 8) | (audioBytes[sampleIndex + 2] & 0xFF);
            leftChannel[i] = left / 32768.0f;
            rightChannel[i] = right / 32768.0f;
        }
    }

    public float[] getLeftChannel() {
        return leftChannel;
    }

    public float[] getRightChannel() {
        return rightChannel;
    }

    public float getSampleRate() {
        return sampleRate;
    }
} 