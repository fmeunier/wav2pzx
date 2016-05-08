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

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TextFileTape {
    /**
     * Processes the samples in the named file and converts them to a PulseList
     *
     * @param fileName the source TXT file
     * @return a PulseList populated with the pulse data from the file.
     * @throws IOException          if there is an error reading the source file
     * @throws NullPointerException if {@code fileName} is null
     * @throws NumberFormatException if first level cannot be mapped to an int
     */
    public static PulseList buildPulseList(String fileName) throws IOException, UnsupportedAudioFileException {
        checkNotNull(fileName, "No input TXT file name supplied");

        return new PulseList(getPulses(fileName), getInitialLevel(fileName), 1);
    }

    private static List<Long> getPulses(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return getPulses(reader.lines());  // UncheckedIOException
        }
    }

    static List<Long> getPulses(Stream<String> lines) {
        checkNotNull(lines, "Null stream supplied");
        return lines
                .filter(s -> !s.isEmpty())
                .map(line -> line.split(" : "))
                .filter(array -> array.length > 1)
                .filter(array -> array[0].matches("\\d+")) // Throw away negative pulses
                .map(array -> Long.valueOf(array[0]))
                .collect(Collectors.toList());
    }

    private static int getInitialLevel(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return getInitialLevel(reader.lines());  // UncheckedIOException
        }
    }

    static int getInitialLevel(Stream<String> lines) {
        checkNotNull(lines, "Null stream supplied");
        Optional<String> pulseLevel = lines
                .filter(s -> !s.isEmpty())
                .findFirst()
                .map(line -> line.split(" : "))
                .filter(array -> array.length > 1)
                .map(array -> array[1]);
        int initialLevel = 0;
        if( pulseLevel.isPresent() && pulseLevel.get().matches("\\d+") ) {
            initialLevel = Integer.parseInt(pulseLevel.get());
            // Needs to be 0 or 1, interpret non-zero values as 1
            initialLevel = initialLevel != 0 ? 1 : 0;
        }
        return initialLevel;
    }
}
