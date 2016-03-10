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
package xyz.meunier.wav2pzx;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.util.List;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Classic 8 bit Sinclair computers like the ZX Spectrum store program data on
 * cassette tapes using square waves forming sequences of pulses.
 * The PZX file format has been developed to represent those tapes in a space 
 * efficient format that can also represent some of the higher-level features
 * typically used in tape loading schemes from that time.
 * <p>
 * The home page is at {@link http://zxds.raxoft.cz/pzx.html}
 * <p>
 * This program reads a WAV format audio sample of a tape recording for these
 * machines and converts it to the efficient PZX format.
 * @author Fredrick Meunier
 */
public class WAV2PZX {
    
    /*
     * Any durations are expressed in T cycles of standard 48k Spectrum CPU.
     * This means one cycle equals 1/3500000 second.
     */
    private static final float TARGET_HZ = (float) 3500000.0;
    
    /**
     * Main entry point for WAV2PZX. Two arguments are expected, first the 
     * source WAV filename and second the destination PZX file name.
     * @param args program arguments, two are expected - the source WAV and the destination PZX file names
     */
    public static void main(String[] args) {
        final String wavFileIn = args[0];
        final String pzxFileOut = args[1];
        
        if(wavFileIn == null || pzxFileOut == null) {
            System.err.println("wav2pzx: usage: wav2pzx <infile> <outfile>");
            return;
        }
        
        try {
            // Read and convert the source WAV file from samples to a list of 0/1 pulses in units of TARGET_HZ
            PulseList pulseList = AudioFileTape.buildPulseList(wavFileIn, TARGET_HZ);
            
            // Analyse the source data and translate into an equivalent list of PZX tape blocks
            List<PZXBlock> pzxTape = LoaderContextImpl.buildPZXTapeList(pulseList);

            Path pzxFile = Paths.get(pzxFileOut);

            // Overwrite the destination file with the extracted PZX data
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(pzxFile))) {
                for(PZXBlock block : pzxTape) {
                    byte[] data = block.getPZXBlockDiskRepresentation();
                    out.write(data, 0, data.length);
                }
            } catch (IOException ex) {
                System.err.println(ex);
                Logger.getLogger(WAV2PZX.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            }
        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println(e);
            Logger.getLogger(WAV2PZX.class.getName()).log(Level.SEVERE, e.toString(), e);
        }
    }
}
