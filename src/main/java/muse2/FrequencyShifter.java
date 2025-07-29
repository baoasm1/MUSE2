package muse2;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.PitchShifter;

public class FrequencyShifter {
    public static float[][] shift(float[] left, float[] right, float sampleRate, float shiftAmountHz) {
        // Calculate pitch shift ratio
        float centerFreq = 1000.0f; // Reference frequency for ratio calculation
        float ratio = (centerFreq - shiftAmountHz) / centerFreq;

        float[][] shifted = new float[2][];
        shifted[0] = shiftChannel(left, sampleRate, ratio);
        shifted[1] = shiftChannel(right, sampleRate, ratio);
        return shifted;
    }

    private static float[] shiftChannel(float[] channel, float sampleRate, float ratio) {
        int bufferSize = 1024;
        int overlap = 256;
        float[] output = new float[channel.length];
        float[] buffer = new float[bufferSize];
        float[] outBuffer = new float[bufferSize];
        int pos = 0;
        PitchShifter shifter = new PitchShifter(ratio, bufferSize, overlap, sampleRate);
        while (pos < channel.length) {
            int len = Math.min(bufferSize, channel.length - pos);
            System.arraycopy(channel, pos, buffer, 0, len);
            AudioEvent event = new AudioEvent(new TarsosDSPAudioFormat(sampleRate, 16, 1, true, false));
            event.setFloatBuffer(buffer);
            shifter.process(event);
            float[] processed = event.getFloatBuffer();
            System.arraycopy(processed, 0, outBuffer, 0, len);
            System.arraycopy(outBuffer, 0, output, pos, len);
            pos += len;
        }
        return output;
    }
} 