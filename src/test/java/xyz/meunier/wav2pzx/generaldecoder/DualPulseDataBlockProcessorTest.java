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

import static com.google.common.collect.Range.singleton;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static xyz.meunier.wav2pzx.generaldecoder.BlockType.*;

public class DualPulseDataBlockProcessorTest {

    private final List<Long> HEADER_DATA_PULSES = asList(
             952L,  476L,  952L,  794L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  794L,
             873L,  794L,  873L,  794L,  873L,  794L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,
             794L,  873L,  794L,  794L,  873L,  794L,  873L,  794L, 1349L, 1825L, 1032L,  635L,  952L,  714L,
            1429L, 1825L, 1667L, 1667L, 1032L,  714L, 1349L, 1905L, 1032L,  635L, 1508L, 1825L, 1032L,  635L,
            1508L, 1746L, 1032L,  714L, 1429L, 1825L, 1032L,  635L, 1508L, 1825L,  952L,  714L, 1429L, 1825L,
            1032L,  714L, 1429L, 1825L, 1032L,  635L,  952L,  794L, 1349L, 1905L,  952L,  714L,  952L,  714L,
            1429L, 1825L, 1032L,  714L,  952L,  714L,  873L,  794L, 1429L, 1825L, 1032L,  635L,  952L,  714L,
             952L,  794L, 1349L, 1905L,  952L,  714L,  952L,  714L,  873L,  794L, 1429L, 1825L, 1032L,  635L,
            1508L, 1825L,  952L,  714L, 1429L, 1905L,  952L,  714L, 1429L, 1825L,  952L,  714L,  952L,  794L,
            1429L, 1825L,  952L,  714L,  952L,  714L,  873L,  794L,  873L,  794L, 1429L, 1825L, 1032L,  635L,
            1587L, 1746L, 1587L, 1746L, 1032L,  714L,  873L,  794L,  873L,  794L,  873L,  873L,  714L,  873L,
             794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,
             794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  794L,
            1429L, 1825L, 1032L,  714L,  952L,  714L,  873L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,
             794L,  794L, 1429L, 1746L, 1746L, 1587L, 1667L, 1746L, 1587L, 1746L, 1587L, 1746L, 1032L,  714L,
             952L,  714L,  873L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  794L,
            1429L, 1825L, 1032L,  714L,  952L,  714L,  873L,  794L,  873L,  873L,  794L,  873L,  794L,  873L,
             794L,  873L,  794L,  873L,  794L,  873L,  794L,  794L,  873L,  873L,  794L,  873L,  794L,  873L,
             794L,  873L,  794L,  873L,  794L,  873L,  794L,  873L,  794L,  794L, 1429L, 1825L, 1111L,  556L,
            1587L, 1825L,  952L,  714L, 1508L, 1825L, 1587L, 1746L, 1032L,  714L,  873L,  794L,  873L,  794L,
             873L,  873L,  794L,  873L,  794L,  873L,  794L,  794L, 1429L, 1825L, 1032L,  635L,  952L,  714L,
            1429L, 1825L, 1667L, 1667L, 1587L, 1746L, 1032L,  714L, 1349L, 1984L,  952L,  714L,  952L, 1825L);

    @Test(expected = NullPointerException.class)
    public void checkNullPulseListIsRejected() throws Exception {
        new DualPulseDataBlockProcessor(null);
    }

    @Test
    public void checkShortListsOfPulsesAreNotEncodedAsADataBlock() throws Exception {
        List<Long> multiSymbolPulseList = asList(1000L, 1100L, 3100L, 3200L);
        PulseList pulseList = new PulseList(multiSymbolPulseList, 0, 1);

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<BitData> multiSymbolAverages = ImmutableList.of(
                new BitData(singleton(1000L), singletonList(1000L)),
                new BitData(singleton(1100L), singletonList(1100L)),
                new BitData(singleton(3100L), singletonList(3100L)),
                new BitData(singleton(3200L), singletonList(3200L))
        );

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        assertThat(tapeBlocks, equalTo(singletonList(new TapeBlock(UNKNOWN, multiSymbolAverages, pulseList))));
    }

    @Test
    public void checkThreeOrMoreRangesAreNotEncodedAsADataBlock() throws Exception {
        List<Long> multiSymbolPulseList = asList(
                1000L, 1100L, 3100L, 3200L, 1000L, 1100L, 3100L, 3200L, 4000L, 4000L,
                1000L, 1100L, 3100L, 3200L, 1000L, 1100L, 3100L, 3200L, 4000L, 4000L
        );
        PulseList pulseList = new PulseList(multiSymbolPulseList, 0, 1);

        List<BitData> multiSymbolAverages = ImmutableList.of(
                new BitData(singleton(1000L), singletonList(1000L)),
                new BitData(singleton(1100L), singletonList(1100L)),
                new BitData(singleton(3100L), singletonList(3100L)),
                new BitData(singleton(3200L), singletonList(3200L)),
                new BitData(singleton(4000L), singletonList(4000L))
        );

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        assertThat(tapeBlocks, equalTo(singletonList(new TapeBlock(UNKNOWN, multiSymbolAverages, pulseList))));
    }

    @Test
    public void checkDataBitsAreReplacedWithAverages() throws Exception {
        PulseList pulseList = new PulseList(HEADER_DATA_PULSES.subList(2, HEADER_DATA_PULSES.size() - 2), 0, 1);

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        assertThat(tapeBlocks.size(), is(1));
        assertThat(tapeBlocks.get(0).getBlockType(), is(DATA));
        assertThat(tapeBlocks.get(0).getPulseList().getPulseLengths().stream().distinct().collect(toList()), is(asList(837L, 1644L)));
    }

    @Test
    public void checkSyncCandidateIsIdentified() throws Exception {
        PulseList pulseList = new PulseList(HEADER_DATA_PULSES, 0, 1);

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        assertThat(tapeBlocks.get(0), is(notNullValue()));
        assertThat(tapeBlocks.get(0).getBlockType(), is(SYNC_CANDIDATE));
        assertThat(tapeBlocks.get(0).getPulseList().getPulseLengths(), is(asList(952L, 476L)));
    }

    @Test
    public void checkTailCandidatesAreIdentified() throws Exception {
        PulseList pulseList = new PulseList(HEADER_DATA_PULSES, 0, 1);

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        if (tapeBlocks.size() < 3) fail("Should identify at least one block prior to the two tail candidates");
        int secondLastBlockIndex = tapeBlocks.size() - 2;
        assertThat(tapeBlocks.get(secondLastBlockIndex), is(notNullValue()));
        assertThat(tapeBlocks.get(secondLastBlockIndex).getBlockType(), is(TAIL_CANDIDATE));
        assertThat(tapeBlocks.get(secondLastBlockIndex).getPulseList().getPulseLengths(), is(singletonList(952L)));
        int lastBlockIndex = tapeBlocks.size() - 1;
        assertThat(tapeBlocks.get(lastBlockIndex), is(notNullValue()));
        assertThat(tapeBlocks.get(lastBlockIndex).getBlockType(), is(TAIL_CANDIDATE));
        assertThat(tapeBlocks.get(lastBlockIndex).getPulseList().getPulseLengths(), is(singletonList(1825L)));
    }

    @Test
    public void checkShortDatablocksBecomeUnknownBlocks() throws Exception {
        // Focus on the case I have in BCs Quest for tires, the sync pulses were being encoded as a two pulse data block
        // when it should have been a sync candidate to be integrated into the pilot tones block
        List<Long> bcsQuestPulses = asList(
                1027L,  632L, 1027L,  948L, 1027L,  948L, 1027L,  948L, 1027L, 1027L,  869L,  790L,  790L,  790L,  790L,
                 711L,  869L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,  790L,
                 790L,  790L,  869L,  711L,  869L,  711L,  869L,  711L,  869L,  711L,  869L,  711L,  869L,  711L,  869L,
                 711L,  869L,  711L,  869L,  711L,  948L, 1264L, 1185L, 1185L, 1027L,  711L, 1027L, 1185L, 1185L, 1185L,
                1027L,  711L, 1027L, 1185L, 1185L, 1185L, 1027L,  711L,  869L,  711L,  869L,  711L, 1027L, 1185L, 1185L,
                1185L, 1027L,  711L, 1027L, 1185L, 1027L,  711L, 1027L, 1185L, 1185L, 1185L, 1185L, 1185L, 1027L,  711L,
                1027L, 1185L
        );

        PulseList pulseList = new PulseList(bcsQuestPulses, 1, 1);

        DualPulseDataBlockProcessor dataBlockProcessor = new DualPulseDataBlockProcessor(pulseList);

        List<TapeBlock> tapeBlocks = dataBlockProcessor.processDataBlock();

        assertThat(tapeBlocks.size(), is(6));
        assertThat(tapeBlocks.get(0).getBlockType(), is(SYNC_CANDIDATE));
        assertThat(tapeBlocks.get(0).getPulseList().getPulseLengths(), is(asList(801L, 801L)));
    }
}