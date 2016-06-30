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
import com.google.common.collect.Range;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static xyz.meunier.wav2pzx.generaldecoder.BlockType.*;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.*;

final class DualPulseDataBlockProcessor {
    private static final int SYNC_OR_TAIL_BUFFER_SIZE = 4;
    private static final int SYNC_AND_TAIL_TOTAL_LIMIT = SYNC_OR_TAIL_BUFFER_SIZE * 2;
    private static final int MINIMUM_DATA_BLOCK_PULSE_COUNT = SYNC_AND_TAIL_TOTAL_LIMIT + 1;
    private static final int THREE_BYTES_OF_PULSES = 24;

    private final PulseList pulseList;
    private final ImmutableList<Long> pulseLengths;
    private final int firstPulseLevel;
    private final long resolution;
    private int hasCandidateTailPulse;
    private Map<Range<Long>, Long> averages;
    private final ArrayList<Long> newDataBlockPulses = new ArrayList<>();
    private final List<TapeBlock> newTapeBlockList = new ArrayList<>();

    DualPulseDataBlockProcessor(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList must not be null");
        this.pulseList = pulseList;
        pulseLengths = pulseList.getPulseLengths();
        hasCandidateTailPulse = pulseLengths.size() % 2;
        firstPulseLevel = pulseList.getFirstPulseLevel();
        resolution = pulseList.getResolution();
    }

    List<TapeBlock> processDataBlock() {
        // Start 4 pulses in to give space to sync pulses - we expect these to be nearly always be an even number
        // End 4 pulses (+1 if there are an odd number of pulses) from the end to leave spaces for tail pulses
        // Iterate through pairs of collection and add sum of pair as a key to a map.
        // Nearly all loaders work with equal pairs of pulses to make 0 and 1 bits, and the summed pair of pulses
        // will be more consistent than the individual pulses. This should help define a range for 0 and 1 pulses
        // Average these out and then assign all classified pairs to the averaged values
        int limit = pulseLengths.size() > MINIMUM_DATA_BLOCK_PULSE_COUNT + THREE_BYTES_OF_PULSES ?
                getPulseLengthsSizeWithoutSyncAndTailArea() : 0;

        ArrayList<Long> fullBits = new ArrayList<>(limit/2);
        for(int i = SYNC_OR_TAIL_BUFFER_SIZE; i < limit; i+=2) {
            Long bit = pulseLengths.get(i) + pulseLengths.get(i+1);
            fullBits.add(bit);
        }

        List<Range<Long>> ranges = getRanges(fullBits.stream().distinct().collect(toList()));

        // If we don't have enough bits to make a good population of dual pulses just dump out the source pulses
        if(ranges.isEmpty()) {
            ranges = RangeFinder.getRangesForSinglePulses(pulseLengths);
            TapeBlock newBlock = new TapeBlock(UNKNOWN, getSingletonPulseLengthsOfRanges(ranges), pulseList);
            getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, newBlock.toString());
            return singletonList(newBlock);
        }

        // Find average of pulse pairs matching each range in this block
        averages = getAveragePulseLengthsOfRanges(ranges, fullBits);

        // If we have more than two ranges we can't encode this block as a data block - just dump out the source pulses
        if(averages.size() != 2) {
            TapeBlock newBlock = new TapeBlock(UNKNOWN, averages, pulseList);
            getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, newBlock.toString());
            return singletonList(newBlock);
        }

        // Track level as we process block. Any pulses that don't match a range should be stored to its own
        // PulseList except for a single tail pulse? Maybe safer to just dump that out too and handle in a second
        // pass through the processed PulseLists (e.g. Unknown -> Pilot -> Sync -> Data -> Tail -> Unknown -> Pilot etc.)

        // We have already assumed that the pulses will be in pairs except for a potential tail pulse, so they succeed
        // or fail on that basis
        for (int i = 0; i < pulseLengths.size() - hasCandidateTailPulse; i += 2) {
            List<Long> bitPulse = new ArrayList<>(2);
            bitPulse.addAll(pulseLengths.subList(i, i + 2));

            if(replaceInRangeBitsWithAverageValues(bitPulse)) {
                newDataBlockPulses.addAll(bitPulse);
            } else {
                // Finish any open PulseList
                finishDataBlock();

                // Add pulses to new PulseList
                if(isInSyncCandidateArea(i)) {
                    // SYNC bit candidates at the beginning of the block
                    addTapeBlockForSinglePulses(SYNC_CANDIDATE, bitPulse, firstPulseLevel);
                } else {
                    // And tail pulse candidates elsewhere
                    addTapeBlockForSinglePulses(TAIL_CANDIDATE, bitPulse.subList(0, 1), firstPulseLevel);
                    addTapeBlockForSinglePulses(TAIL_CANDIDATE, bitPulse.subList(1, 2), flipPulseLevel(firstPulseLevel));
                }
            }
        }

        finishDataBlock();

        processCandidateTailPulse();

        getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, newTapeBlockList.toString());
        return newTapeBlockList;
    }

    private int getPulseLengthsSizeWithoutSyncAndTailArea() {
        return (pulseLengths.size() - SYNC_AND_TAIL_TOTAL_LIMIT) - hasCandidateTailPulse;
    }

    private void finishDataBlock() {
        // Finish any open PulseList
        if(!newDataBlockPulses.isEmpty()) {
            addTapeBlock(DATA, averages, newPulseList(newDataBlockPulses, firstPulseLevel));
            newDataBlockPulses.clear();
        }
    }

    private void processCandidateTailPulse() {
        if(hasCandidateTailPulse == 1) {
            int size = pulseLengths.size();
            addTapeBlockForSinglePulses(TAIL_CANDIDATE, pulseLengths.subList(size - 1, size), flipPulseLevel(firstPulseLevel));
        }
    }

    private boolean replaceInRangeBitsWithAverageValues(List<Long> bitPulse) {
        checkNotNull(bitPulse, "bitPulse must not be null");
        checkArgument(bitPulse.size() == 2, "bitPulse must have two entries");

        Long bit = bitPulse.get(0) + bitPulse.get(1);
        for (Map.Entry<Range<Long>, Long> entry : averages.entrySet()) {
            if (entry.getKey().contains(bit)) {
                // Transform bit pulses
                Long newBit = entry.getValue()/2;
                bitPulse.set(0, newBit);
                bitPulse.set(1, newBit);
                return true;
            }
        }
        return false;
    }

    private boolean isInSyncCandidateArea(int index) {
        return index < SYNC_OR_TAIL_BUFFER_SIZE;
    }

    private void addTapeBlockForSinglePulses(BlockType blockType, List<Long> bitPulseList, int firstPulseLevel) {
        addTapeBlock(blockType, getSyncRange(getRangesForSinglePulses(bitPulseList)), newPulseList(bitPulseList, firstPulseLevel));
    }

    private void addTapeBlock(BlockType blockType, Map<Range<Long>, Long> syncRange, PulseList pulseList) {
        newTapeBlockList.add(new TapeBlock(blockType, syncRange, pulseList));
    }

    private PulseList newPulseList(Iterable<Long> pulses, int firstPulseLevel) {
        return new PulseList(pulses, firstPulseLevel, resolution);
    }

    private static int flipPulseLevel(int pulseLevel) {
        return pulseLevel == 0 ? 1 : 0;
    }

    private static Map<Range<Long>, Long> getSyncRange(List<Range<Long>> ranges) {
        return ranges.stream().distinct().collect(toMap(identity(), Range::lowerEndpoint));
    }

}