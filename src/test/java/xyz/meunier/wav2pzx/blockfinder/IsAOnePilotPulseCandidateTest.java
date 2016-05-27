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

package xyz.meunier.wav2pzx.blockfinder;

import com.google.common.collect.Range;
import org.junit.Test;
import xyz.meunier.wav2pzx.PulseList;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static xyz.meunier.wav2pzx.blockfinder.RangeFinder.getRanges;

public class IsAOnePilotPulseCandidateTest {

    private final List<Long> pulseLengthList = singletonList(200L);
    private final PulseList pulseList = new PulseList(pulseLengthList, 0, 1);
    private final Map<Range<Long>, Long> pulseLengthListAverages = getAveragePulseLengthsOfRanges(pulseLengthList);
    private final TapeBlock pulseListTapeBlock = new TapeBlock(BlockType.UNKNOWN, pulseLengthListAverages, pulseList);

    private final List<Long> pilotPulsesList = asList((long) LoaderContext.PILOT_LENGTH - 50, (long) LoaderContext.PILOT_LENGTH + 50);
    private final PulseList pilotPulses = new PulseList(pilotPulsesList, 1, 1);
    private final Map<Range<Long>, Long> pilotPulsesAverages = getAveragePulseLengthsOfRanges(pilotPulsesList);
    private final TapeBlock pilotPulsesTapeBlock = new TapeBlock(BlockType.UNKNOWN, pilotPulsesAverages, pilotPulses);

    private final List<Long> pilotPulse = singletonList((long) LoaderContext.PILOT_LENGTH);
    private final PulseList exactPilot = new PulseList(pilotPulse, 1, 1);
    private final Map<Range<Long>, Long> pilotPulseAverage = getAveragePulseLengthsOfRanges(pilotPulse);
    private final TapeBlock pilotPulseTapeBlock = new TapeBlock(BlockType.UNKNOWN, pilotPulseAverage, exactPilot);

    private final IsAOnePilotPulseCandidate instance = new IsAOnePilotPulseCandidate();

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfTapeBlockIsNull() {
        instance.test(null);
    }

    @Test
    public void shouldReturnFalseIfSizeIsTooLarge() {
        assertThat(instance.test(pilotPulsesTapeBlock), is(false));
    }

    @Test
    public void shouldReturnFalseIfPulseIsWrongSize() {
        assertThat(instance.test(pulseListTapeBlock), is(false));
    }

    @Test
    public void shouldReturnTrueIfSizeIsOneAndPulseIsRightSize() {
        assertThat(instance.test(pilotPulseTapeBlock), is(true));
    }

    private static Map<Range<Long>, Long> getAveragePulseLengthsOfRanges(List<Long> pulseLengthList) {
        return RangeFinder.getAveragePulseLengthsOfRanges(getRanges(pulseLengthList), pulseLengthList);
    }

}
