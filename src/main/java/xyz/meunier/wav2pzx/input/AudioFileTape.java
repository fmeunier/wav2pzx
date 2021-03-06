/*
 * Copyright (c) 2016, Fredrick Meunier
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xyz.meunier.wav2pzx.input;

import xyz.meunier.wav2pzx.input.triggers.Bistable;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.newInputStream;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioSystem.isConversionSupported;

/**
 * This class handles reading a WAV tape sample file and converting them to the
 * PulseList format. The source data represents logical 0 and 1 level pulse values.
 * @author Fredrick Meunier
 */
public final class AudioFileTape {
    
    /**
     * Processes the samples in the named file and converts them to a PulseList
     * resampled to a base of targetHz.
     * @param fileName the source WAV file
     * @param targetHz the base rate for the resultant pulses
     * @param trigger determines when the signal level of a sample should be 0 or 1
     * @return a PulseList populated with the pulse data from the file.
     * @throws java.io.IOException if there is an error reading the source file
     * @throws javax.sound.sampled.UnsupportedAudioFileException if the WAV file cannot be converted to the required format
     * @throws NullPointerException if {@code fileName} is null
     */
    public static PulseList buildPulseList(String fileName, float targetHz, Bistable trigger)
            throws IOException, UnsupportedAudioFileException {
        checkNotNull(fileName, "No input WAV file name supplied");
        
        int totalFramesRead = 0;
        
        // Open WAV file specified on the command line
        Path fileIn = Paths.get(fileName);

        try (InputStream inputStream = newInputStream(fileIn)) {
            // Add buffer for mark/reset support
            AudioInputStream audioInputStream = getAudioInputStream(new BufferedInputStream(inputStream));
            AudioFormat inDataFormat = audioInputStream.getFormat();

            // Process the WAV as a mono 8 bit file to limit memory requirements
            AudioFormat dataFormat = 
                    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 
                            inDataFormat.getSampleRate(), 8, 1, 1, 
                            inDataFormat.getFrameRate(), 
                            inDataFormat.isBigEndian());
            
            if (!isConversionSupported(dataFormat, inDataFormat)) {
                throw new UnsupportedAudioFileException("Unsupported WAV audio format " + inDataFormat.toString());
            }

            System.out.println("Using WAV format " + dataFormat.toString());
            
            AudioInputStream lowResAIS = getAudioInputStream(dataFormat, audioInputStream);

            AudioSamplePulseListBuilder pulseListBuilder =
                    new AudioSamplePulseListBuilder(inDataFormat.getSampleRate(), targetHz, trigger);
            
            int bytesPerFrame = 1;
            int numBytes = 1024 * bytesPerFrame; 
            byte[] audioBytes = new byte[numBytes];

            int numBytesRead;
            int numFramesRead;
            
            // Try to read numBytes bytes from the file.
            while ((numBytesRead = lowResAIS.read(audioBytes)) != -1) {
                // Calculate the number of frames actually read.
                numFramesRead = numBytesRead / bytesPerFrame;
                totalFramesRead += numFramesRead;
                // Here, do something useful with the audio data that's 
                // now in the audioBytes array...
                for(int i = 0; i<numBytesRead; i++) {
                    pulseListBuilder.addSample(0x000000FF & ((int)audioBytes[i]));
                }
            }

            Logger.getLogger(AudioFileTape.class.getName())
                    .log(Level.FINE, String.format("Processed %s samples", totalFramesRead));
            
            return pulseListBuilder.build();
        }
    }

}
