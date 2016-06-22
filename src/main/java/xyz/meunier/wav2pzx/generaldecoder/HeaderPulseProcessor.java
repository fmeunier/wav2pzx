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

package xyz.meunier.wav2pzx.generaldecoder;

import com.google.common.collect.Range;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static xyz.meunier.wav2pzx.generaldecoder.BlockType.PILOT;
import static xyz.meunier.wav2pzx.generaldecoder.BlockType.UNKNOWN;
import static xyz.meunier.wav2pzx.generaldecoder.LoaderContext.isaPilotCandidate;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.getAveragePulseLengthsOfRanges;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.getRanges;

/**
 * This class has helper methods to construct the various kinds of pulse blocks.
 */
final class HeaderPulseProcessor {

    private HeaderPulseProcessor() {
    }

    /**
     * Analyse the supplied pulse list and construct a suitable TapeBlock. If the block appears to be a pilot block, the
     * pulses will be replaced with average values of all the pulses in the block.
     * @param pulseList the pulses to process
     * @return a TapeBlock representing the supplied pulses
     */
    static TapeBlock processPulseBlock(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList cannot be null");

        Collection<Long> pulseLengths = pulseList.getPulseLengths();
        List<Range<Long>> ranges = getRanges(pulseLengths);

//        System.out.println(ranges.toString());

        Map<Range<Long>, Long> averages = getAveragePulseLengthsOfRanges(ranges, pulseLengths);
        BlockType blockType = UNKNOWN;

        // Use average pulse length for range if there is only one (and this is likely a pilot block)
        Long average = averages.get(ranges.get(0));
        if (ranges.size() == 1 && isaPilotCandidate(average)) {
            blockType = PILOT;

//            System.out.println(averages.toString());

            // Replace all pulses with the average
            pulseLengths = pulseLengths.stream().map(p -> average).collect(toList());

            pulseList = new PulseList(pulseLengths, pulseList.getFirstPulseLevel(), pulseList.getResolution());
        }

        return new TapeBlock(blockType, averages, pulseList);
    }

}