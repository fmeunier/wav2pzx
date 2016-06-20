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
import xyz.meunier.wav2pzx.PulseListBuilder;
import xyz.meunier.wav2pzx.blocks.PZXBlock;
import xyz.meunier.wav2pzx.blocks.PZXDataBlock;
import xyz.meunier.wav2pzx.blocks.PZXHeaderBlock;
import xyz.meunier.wav2pzx.blocks.PZXPulseBlock;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static xyz.meunier.wav2pzx.blockfinder.BlockType.*;
import static xyz.meunier.wav2pzx.blockfinder.PZXBuilder.buildPZXTapeList;
import static xyz.meunier.wav2pzx.blockfinder.RangeFinder.getAveragePulseLengthsOfRanges;
import static xyz.meunier.wav2pzx.blockfinder.RangeFinder.getRanges;

public class PZXBuilderTest {

    private List<Long> pulseLengths = asList(1L, 1L);
    private PulseList pulseList = new PulseListBuilder()
            .withFirstPulseLevel(0)
            .withNextPulses(pulseLengths)
            .build();
    private PZXPulseBlock pzxPulseBlock = new PZXPulseBlock(pulseList);
    private PZXHeaderBlock pzxHeaderBlock = new PZXHeaderBlock();

    @Test
    public void shouldGetAPZXPulseBlockFromAnUnknownBlock() throws Exception {
        // Unknown blocks become pulse blocks
        testAPZXPulseBlockOfType(UNKNOWN);
    }

    @Test
    public void shouldGetAPZXPulseBlockFromASyncCandidateBlock() throws Exception {
        // Lone sync candidate blocks become pulse blocks
        testAPZXPulseBlockOfType(SYNC_CANDIDATE);
    }

    @Test
    public void shouldGetAPZXPulseBlockFromATailCandidateBlock() throws Exception {
        // Lone tail candidate blocks become pulse blocks
        testAPZXPulseBlockOfType(TAIL_CANDIDATE);
    }

    @Test
    public void shouldGetAPZXPulseBlockFromAPilotBlock() throws Exception {
        // A pilot block becomes a pulse block
        testAPZXPulseBlockOfType(PILOT);
    }

    // A pilot block followed by any other block becomes a pulse block except when it is followed by a sync candidate
    // when it is coalesced with it first
    @Test
    public void shouldCoalescePilotBlockWithFollowingSyncCandidate() throws Exception {
        List<TapeBlock> blockList = asList(getTapeBlock(PILOT, pulseList), getTapeBlock(SYNC_CANDIDATE, pulseList));

        List<PZXBlock> pzxBlocks = buildPZXTapeList(blockList);

        assertThat(pzxBlocks, is(notNullValue()));
        assertThat(pzxBlocks, is(asList(pzxHeaderBlock, new PZXPulseBlock(new PulseList(pulseList, pulseList)))));
    }

    @Test
    public void shouldNotCoalescePilotBlockWithFollowingOtherBlock() throws Exception {
        complementOf(of(SYNC_CANDIDATE, DATA)).stream()
                .forEach(this::testANonCoalescingPilotAndFollowingBlockCombination);

        // Also test handling of DATA block following PILOT block
        testNonCoalesceOfBlockWithFollowingDataBlock(PILOT, pzxPulseBlock);
    }

    // A data block ignores following blocks except for a candidate tail that will be coalesced, it turns pairs of the
    // lowest value into 0s and the highest pair into 1s
    @Test
    public void shouldCoalesceDataBlockWithFollowingTailCandidate() throws Exception {
        List<TapeBlock> blockList = asList(
                getTapeBlock(DATA, pulseList),
                getTapeBlock(TAIL_CANDIDATE, new PulseList(singletonList(5L), 0, 1)));

        List<PZXBlock> pzxBlocks = buildPZXTapeList(blockList);

        assertThat(pzxBlocks, is(notNullValue()));
        assertThat(pzxBlocks, is(asList(pzxHeaderBlock, getPzxDataBlock(5L))));
    }

    @Test
    public void shouldNotCoalesceDataBlockWithFollowingOtherBlock() throws Exception {
        complementOf(of(TAIL_CANDIDATE, DATA)).stream()
                .forEach(this::testANonCoalescingDataAndFollowingBlockCombination);

        // Also test handling of DATA block following DATA block
        testNonCoalesceOfBlockWithFollowingDataBlock(DATA, getPzxDataBlock(0));
    }

    @Test
    public void shouldAlternatePulseLevelsBetweenBlocks() throws Exception {
        int initialLevel = 0;
        List<Long> pulses = asList(15946112L, 2101L, 2101L, 2101L, 2101L, 2101L, 2101L);
        PulseList pulseList = new PulseList(pulses, initialLevel, 1L);

        List<PZXBlock> blocks = buildPZXTapeList(pulseList);

        assertThat(blocks.size(), is(3));
        assertThat(blocks.get(1).getPulses().size(), is(1));
        assertThat(blocks.get(1).getFirstPulseLevel(), is(initialLevel));
        assertThat(blocks.get(1).getFirstPulseLevel(), is(not(blocks.get(2).getFirstPulseLevel())));
    }

    private void testNonCoalesceOfBlockWithFollowingDataBlock(BlockType type, PZXBlock pzxBlock) {
        List<TapeBlock> blockList = asList(getTapeBlock(type, pulseList), getTapeBlock(DATA, pulseList));

        List<PZXBlock> pzxBlocks = buildPZXTapeList(blockList);

        assertThat(pzxBlocks, is(notNullValue()));
        assertThat(pzxBlocks, is(asList(pzxHeaderBlock, pzxBlock, getPzxDataBlock(0))));
    }

    private void testANonCoalescingDataAndFollowingBlockCombination(BlockType type) {
        PulseList pulseList1 = new PulseList(singletonList(5L), 0, 1);
        List<TapeBlock> blockList = asList(
                getTapeBlock(DATA, this.pulseList),
                getTapeBlock(type, pulseList1));

        List<PZXBlock> pzxBlocks = buildPZXTapeList(blockList);

        assertThat(pzxBlocks, is(notNullValue()));
        assertThat(pzxBlocks, is(asList(pzxHeaderBlock, getPzxDataBlock(0L), new PZXPulseBlock(pulseList1))));
    }

    private PZXDataBlock getPzxDataBlock(long tailLength) {
        return new PZXDataBlock(pulseList, 1L, 1L, tailLength, 1, singletonList((byte)0));
    }

    private void testANonCoalescingPilotAndFollowingBlockCombination(BlockType type) {
        List<TapeBlock> blockList = asList(getTapeBlock(PILOT, pulseList), getTapeBlock(type, pulseList));

        List<PZXBlock> pzxBlocks = buildPZXTapeList(blockList);

        assertThat(pzxBlocks, is(notNullValue()));
        assertThat(pzxBlocks, is(asList(pzxHeaderBlock, pzxPulseBlock, pzxPulseBlock)));
    }

    private void testAPZXPulseBlockOfType(BlockType type) {
        List<PZXBlock> blockList = getPzxBlocksOfType(type, pulseList);

        assertThat(blockList, is(notNullValue()));
        assertThat(blockList, is(asList(pzxHeaderBlock, pzxPulseBlock)));
    }

    private List<PZXBlock> getPzxBlocksOfType(BlockType type, PulseList pulseList) {
        TapeBlock tapeBlock = getTapeBlock(type, pulseList);

        return buildPZXTapeList(singletonList(tapeBlock));
    }

    private TapeBlock getTapeBlock(BlockType type, PulseList pulseList) {
        List<Long> fullPulses = singletonList(2L);
        List<Range<Long>> ranges = getRanges(fullPulses);
        Map<Range<Long>, Long> rangeAverages = getAveragePulseLengthsOfRanges(ranges, fullPulses);

        return new TapeBlock(type, rangeAverages, pulseList);
    }

}