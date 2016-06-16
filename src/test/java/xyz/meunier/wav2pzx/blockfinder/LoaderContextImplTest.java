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

import org.junit.Test;
import xyz.meunier.wav2pzx.PulseList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static xyz.meunier.wav2pzx.blockfinder.BlockType.PILOT;
import static xyz.meunier.wav2pzx.blockfinder.BlockType.UNKNOWN;

public class LoaderContextImplTest {

    private List<Long> pulses = Arrays.asList(
            2143L, 2460L, 1667L, 2302L, 2143L, 1984L, 2143L, 2143L, 1984L, 2302L, 1905L, 2381L, 1825L, 2302L, 1984L,
            2302L, 1905L, 2302L, 1905L, 2302L, 1905L, 2302L, 1984L, 2222L, 1984L, 2302L, 1905L, 2302L, 1905L, 2302L,
            1905L, 2302L, 1984L, 2222L, 1984L, 2302L, 1905L, 2302L, 1905L, 2302L, 1984L, 2222L, 1984L, 2302L, 1905L,
            2222L, 1984L, 2222L, 1984L, 2222L, 1984L, 2222L, 1984L, 2222L, 1984L, 2302L, 1905L, 2302L, 1905L, 2222L,
            1984L, 2222L, 1984L, 2302L, 1905L, 2222L, 1984L, 2222L, 1984L, 2222L, 1905L, 2302L, 1905L, 2302L, 1905L,
            2302L, 1905L, 2302L, 1825L);
    private final PulseList pulseList = new PulseList(pulses, 0, 1);
    private final LoaderContextImpl instance = new LoaderContextImpl(pulseList);

    @Test
    public void buildTapeBlockList() throws Exception {
        List<Long> sourcePulses = Arrays.asList(200L, 200L, 200L);
        PulseList pulseList = new PulseList(sourcePulses, 1, 1);
        List<TapeBlock> result = LoaderContextImpl.buildTapeBlockList(pulseList);
        List<Long> pulses = new ArrayList<>();
        result.stream().forEach((block) -> pulses.addAll(block.getPulseList().getPulseLengths()));

        assertThat("Pulses from generated tape should match source", pulses, equalTo(sourcePulses));
    }

    @Test
    public void getNextPulse() throws Exception {
        assertThat("getNextPulse() returns the next pulse", instance.getNextPulse(), is(pulses.get(0)));
    }

    @Test
    public void hasNextPulse() throws Exception {
        PulseList pulseList = new PulseList(singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        assertThat("Check hasNextPulse is true when there is another pulse", instance.hasNextPulse(), is(true));

        instance.getNextPulse();
        assertThat("Check hasNextPulse is false when there is not another pulse", instance.hasNextPulse(), is(false));
    }

    @Test
    public void getCurrentPulse() throws Exception {
        instance.getNextPulse();
        assertThat("Check getCurrentPulse returns the current pulse", instance.getCurrentPulse(), is(pulses.get(0)));
    }

    @Test
    public void peekNextPulse() throws Exception {
        assertThat("Check peekNextPulse looks at the next pulse", instance.peekNextPulse(), is(pulses.get(0)));
    }

    @Test
    public void getResolution() throws Exception {
        assertThat("Check getResolution returns the resolution in use", instance.getResolution(), is(1L));
    }

    @Test
    public void checkThatAPilotPulseIsCountedAndStoredInAPilotTapeBlock() throws Exception {
        Long pilotPulseLength = 2100L;
        instance.addPilotPulse(pilotPulseLength);

        assertThat("Check pilot pulse recorded", instance.getNumPilotPulses(), equalTo(1));

        instance.completePilotPulseBlock();
        List<TapeBlock> tapeBlockList = instance.getTapeBlockList();

        assertThat("Check one block is created", tapeBlockList.size(), is(1));
        TapeBlock tapeBlock = tapeBlockList.get(0);
        assertThat("Check the block has the proper type", tapeBlock.getBlockType(), is(PILOT));
        assertThat("Check the block has the supplied pilot pulse ", tapeBlock.getPulseList().getPulseLengths(),
                is(singletonList(pilotPulseLength)));
    }

    @Test
    public void checkThatAnUnclassifiedPulseIsStoredInAnUnknownTapeBlock() throws Exception {
        // Really want to check that a unclassified block ends up in a proper TapeBlock
        Long pulseLength = 50L;
        instance.addUnclassifiedPulse(pulseLength);
        instance.completeUnknownPulseBlock();
        List<TapeBlock> tapeBlockList = instance.getTapeBlockList();

        assertThat("Check one block is created", tapeBlockList.size(), is(1));
        TapeBlock tapeBlock = tapeBlockList.get(0);
        assertThat("Check the block has the proper type", tapeBlock.getBlockType(), is(UNKNOWN));
        assertThat("Check the block has the supplied pulse ", tapeBlock.getPulseList().getPulseLengths(),
                is(singletonList(pulseLength)));
    }

    @Test
    public void revertCurrentBlockDoesntBreakBlockPrecedingEmptyBlock() throws Exception {
        // Completing then reverting a block should put its pulses in the current block
        // If the completed block was empty then it should still be removed from the builder

        // Test 1 - should result in one block with original pulse
        // 1 add a pulse
        // 2 complete block
        // 3 add no pulses
        // 4 revert block
        // 5 complete tape
        Long pulseLength = 50L;

        instance.addUnclassifiedPulse(pulseLength);
        instance.completeUnknownPulseBlock();
        instance.completeUnknownPulseBlock();
        instance.revertCurrentBlock();
        List<TapeBlock> tapeBlockList = instance.getTapeBlockList();

        assertThat("A tape block list was returned", tapeBlockList, is(notNullValue()));
        assertThat("One tape block was made", tapeBlockList.size(), is(1));
        TapeBlock tapeBlock = tapeBlockList.get(0);
        assertThat("Check the block has the proper type", tapeBlock.getBlockType(), is(UNKNOWN));
        assertThat("Check the block has the supplied pulse ", tapeBlock.getPulseList().getPulseLengths(),
                is(singletonList(pulseLength)));
    }

    @Test
    public void revertCurrentBlockShouldAddCurrentPulsesToPreviousNonEmptyBlock() throws Exception {
        // Test 2 - should result in one block with two pulses
        // 1 add a pulse
        // 2 complete block
        // 3 revert block add pulse
        // 4 complete block
        // 5 complete tape
        Long pulseLength = 50L;

        instance.addUnclassifiedPulse(pulseLength);
        instance.completeUnknownPulseBlock();
        instance.addUnclassifiedPulse(pulseLength);
        instance.revertCurrentBlock();
        instance.completeUnknownPulseBlock();
        List<TapeBlock> tapeBlockList = instance.getTapeBlockList();

        assertThat("A tape block list was returned", tapeBlockList, is(notNullValue()));
        assertThat("One tape block was made", tapeBlockList.size(), is(1));
        Collection<Long> pulseLengths = tapeBlockList.get(0).getPulseList().getPulseLengths();
        assertThat("We have the expected 2 pulses in the block", pulseLengths.size(), is(2));
        for (Long length : pulseLengths) {
            assertThat("All pulses should match the source pulse", pulseLength, is(length));
        }
    }

    @Test
    public void getNumPilotPulses() throws Exception {
        Long pulseLength = 50L;
        instance.addPilotPulse(pulseLength);
        instance.addPilotPulse(pulseLength);

        assertThat("We have the expected 2 pilot pulses counted", instance.getNumPilotPulses(), is(2));
    }

}