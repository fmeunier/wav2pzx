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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static xyz.meunier.wav2pzx.generaldecoder.HeaderPulseProcessor.processPulseBlock;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.getRanges;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.getReplacementBitDataOfRanges;

public class HeaderPulseProcessorTest {

    private final List<Long> pulseLengthList = singletonList(200L);
    private final PulseList pulseList = new PulseList(pulseLengthList, 0, 1);
    private final List<BitData> pulseLengthListAverages = getReplacementBitDataOfRanges(getRanges(pulseLengthList), pulseLengthList);
    private final TapeBlock pulseListTapeBlock = new TapeBlock(BlockType.UNKNOWN, pulseLengthListAverages, pulseList);

    private final PulseList pilotPulses = new PulseList(asList((long)LoaderContext.PILOT_LENGTH-50, (long)LoaderContext.PILOT_LENGTH+50), 1, 1);
    private final PulseList exactPilots = new PulseList(asList((long) LoaderContext.PILOT_LENGTH, (long) LoaderContext.PILOT_LENGTH), 1, 1);
    private final List<Long> pilotAnd200List = asList((long) LoaderContext.PILOT_LENGTH, 200L);
    private final PulseList twoRangePulses = new PulseList(pilotAnd200List, 1, 1);

    @Test(expected = NullPointerException.class)
    public void checkProcessPulseBlockThrowsNullPointerExceptionWhenPulseListIsNull() {
        processPulseBlock(null);
    }

    @Test
    public void checkProcessPulseBlockReturnsUnknownBlockWithOriginalPulsesForNonPilotPulseList() {
        assertThat(processPulseBlock(pulseList), is(pulseListTapeBlock));
    }

    @Test
    public void checkProcessPulseBlockReturnsUnknownBlockWithOriginalPulsesForTooManyRanges() {
        ImmutableList<Long> lengths = twoRangePulses.getPulseLengths();
        List<BitData> averages = getReplacementBitDataOfRanges(getRanges(lengths), lengths);
        assertThat(processPulseBlock(twoRangePulses), is(new TapeBlock(BlockType.UNKNOWN, averages, twoRangePulses)));
    }

    @Test
    public void checkProcessPulseBlockReturnsPilotBlockWithAveragedPulsesForNonPilotPulseList() {
        ImmutableList<Long> lengths = pilotPulses.getPulseLengths();
        List<BitData> averages = getReplacementBitDataOfRanges(getRanges(lengths), lengths);
        assertThat(processPulseBlock(pilotPulses), is(new TapeBlock(BlockType.PILOT, averages, exactPilots)));
    }

}