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

import xyz.meunier.wav2pzx.blockfinder.PZXBuilder;
import xyz.meunier.wav2pzx.blocks.PZXBlock;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classic 8 bit Sinclair computers like the ZX Spectrum store program data on
 * cassette tapes using square waves forming sequences of pulses.
 * The PZX file format has been developed to represent those tapes in a space
 * efficient format that can also represent some of the higher-level features
 * typically used in tape loading schemes from that time.
 * <p>
 * The specification is at the <a href="http://zxds.raxoft.cz/pzx.html">PZX home page</a>
 * <p>
 * This program reads a WAV format audio sample of a tape recording for these
 * machines and converts it to the efficient PZX format.
 *
 * @author Fredrick Meunier
 */
public class WAV2PZX {

    private static boolean dumpPulses = false;

    private enum EncodingVersion {
        V10,
        V20
    }

    private static final EncodingVersion version = EncodingVersion.V20;

    /*
     * Any durations are expressed in T cycles of standard 48k Spectrum CPU.
     * This means one cycle equals 1/3500000 second.
     */
    private static final float TARGET_HZ = (float) 3500000.0;

    /**
     * Main entry point for WAV2PZX. Two arguments are expected, first the
     * source WAV filename and second the destination PZX file name.
     *
     * @param args program arguments, two are expected - the source WAV and the destination PZX file names
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }

        final String fileIn = args[0];
        final String pzxFileOut = args[1];

        if (fileIn.isEmpty() || pzxFileOut.isEmpty()) {
            usage();
            return;
        }

        try {
            // Read and convert the source WAV file from samples to a list of 0/1 pulses in units of TARGET_HZ
            PulseList pulseList;
            if (fileIn.toLowerCase().endsWith(".wav")) {
                // Read and convert the source WAV file from samples to a list of 0/1 pulses in units of TARGET_HZ
                pulseList = AudioFileTape.buildPulseList(fileIn, TARGET_HZ);
            } else if (fileIn.toLowerCase().endsWith(".txt")) {
                pulseList = TextFileTape.buildPulseList(fileIn);
            } else {
                usage();
                return;
            }

            // Analyse the source data and translate into an equivalent list of PZX tape blocks
            List<PZXBlock> pzxTape =
                    version == EncodingVersion.V20 ?
                            PZXBuilder.buildPZXTapeList(pulseList) :
                            LoaderContextImpl.buildPZXTapeList(pulseList);

            if (dumpPulses) {
                dumpPulses(pzxTape);
            }

            writePzxFile(pzxFileOut, pzxTape);
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file " + fileIn + ": " + e.getMessage());
            usage();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file " + fileIn + ": " + e.getMessage());
            Logger.getLogger(WAV2PZX.class.getName()).log(Level.FINE, e.toString(), e);
        } catch (IOException e) {
            System.err.println("Error with file " + fileIn + ": " + e.toString());
            Logger.getLogger(WAV2PZX.class.getName()).log(Level.FINE, e.toString(), e);
        }
    }

    private static void dumpPulses(List<PZXBlock> pzxTape) throws IOException {
        try (OutputStream pulses = new BufferedOutputStream(Files.newOutputStream(Paths.get("pulseDump.txt")))) {
            for (PZXBlock block : pzxTape) {
                for (Long pulse : block.getPulses()) {
                    pulses.write(String.format("%d%n", pulse).getBytes());
                }
            }
        }
    }

    private static void writePzxFile(String pzxFileOut, Iterable<PZXBlock> pzxTape) {
        Path pzxFile = Paths.get(pzxFileOut);

        // Overwrite the destination file with the extracted PZX data
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(pzxFile))) {
            for (PZXBlock block : pzxTape) {
                System.out.println(block.getSummary());
                out.write(block.getPZXBlockDiskRepresentation());
            }
        } catch (IOException ex) {
            System.err.println("Error writing file " + pzxFileOut + ": " + ex.getMessage());
            Logger.getLogger(WAV2PZX.class.getName()).log(Level.FINE, ex.toString(), ex);
        }
    }

    private static void usage() {
        System.err.println("wav2pzx: usage: wav2pzx <infile.wav or txt> <outfile.pzx>");
    }
}
