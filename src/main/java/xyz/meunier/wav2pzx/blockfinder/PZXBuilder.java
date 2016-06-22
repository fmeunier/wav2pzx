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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.PeekingIterator;
import xyz.meunier.wav2pzx.blocks.*;
import xyz.meunier.wav2pzx.databuilder.DataBuilder;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import static com.google.common.collect.Iterators.peekingIterator;
import static xyz.meunier.wav2pzx.blockfinder.BlockType.SYNC_CANDIDATE;
import static xyz.meunier.wav2pzx.blockfinder.BlockType.TAIL_CANDIDATE;

/**
 * Utility class for building PZX tape images
 */
public final class PZXBuilder {

    private PZXBuilder() {
    }

    /**
     * Builder method to construct a series of PZXBlocks that represents the data in the supplied PulseList.
     *
     * @param pulseList the tape data to analyse
     * @return the analysed tape image
     */
    public static List<PZXBlock> buildPZXTapeList(PulseList pulseList) {
        // Analyse the source data and translate into an equivalent list of PZX tape blocks
        List<TapeBlock> blocks = LoaderContextImpl.buildTapeBlockList(pulseList);
        return buildPZXTapeList(blocks);
    }

    /**
     * Builder method to construct a series of PZXBlocks that represents the data in the supplied TapeBlock list.
     *
     * @param tapeBlockList the tape data to analyse
     * @return the analysed tape image
     */
    public static List<PZXBlock> buildPZXTapeList(List<TapeBlock> tapeBlockList) {
        List<PZXBlock> pzxTape = new ArrayList<>();
        pzxTape.add(new PZXHeaderBlock());

        PeekingIterator<TapeBlock> iterator = peekingIterator(tapeBlockList.iterator());

        while (iterator.hasNext()) {
            pzxTape.add(getPzxBlock(iterator));
        }

        return pzxTape;
    }

    private static PZXBlock getPzxBlock(PeekingIterator<TapeBlock> iterator) {
        TapeBlock block = iterator.next();
        PulseList blockPulseList = block.getPulseList();
        PZXBlock pzxBlock = new PZXNullBlock();
        switch (block.getBlockType()) {
            case UNKNOWN:
                // TODO if this is the beginning of the tape and followed by a pilot discard
                // TODO if this is about a second after a data block with a tail but before a pulse block use a pause block?
                pzxBlock = new PZXPulseBlock(blockPulseList);
                break;
            case PILOT:
                pzxBlock = getPzxPulseBlock(iterator, blockPulseList);
                break;
            case SYNC_CANDIDATE:
                pzxBlock = new PZXPulseBlock(blockPulseList);
                break;
            case DATA:
                pzxBlock = getPzxDataBlock(iterator, block);
                break;
            case TAIL_CANDIDATE:
                pzxBlock = new PZXPulseBlock(blockPulseList);
                break;
        }
        return pzxBlock;
    }

    private static PZXDataBlock getPzxDataBlock(PeekingIterator<TapeBlock> iterator, TapeBlock block) {
        long tailLength = getTailLength(iterator);

        long zeroPulseLength = getPulseLength(block, Long::min);
        long onePulseLength = getPulseLength(block, Long::max);
        DataBuilder dataBuilder = new DataBuilder();

        ImmutableList<Long> pulseLengths = block.getPulseList().getPulseLengths();
        for (int i = 0; i < pulseLengths.size(); i += 2) {
            Long pulse1 = pulseLengths.get(i);
            Long pulse2 = pulseLengths.get(i + 1);
            if (isSpecifiedPulsePair(zeroPulseLength, pulse1, pulse2)) {
                dataBuilder.addBit(0);
            } else if (isSpecifiedPulsePair(onePulseLength, pulse1, pulse2)) {
                dataBuilder.addBit(1);
            }
            // FIXME: Some kind of error, fall back to PulseBlock?
        }

        int numBitsInLastByte = dataBuilder.getNumBitsInCurrentByte();
        return new PZXDataBlock(block.getPulseList(), zeroPulseLength, onePulseLength, tailLength,
                numBitsInLastByte, dataBuilder.getData());
    }

    private static long getTailLength(PeekingIterator<TapeBlock> iterator) {
        long tailLength = 0;
        if (iterator.hasNext() && iterator.peek().getBlockType() == TAIL_CANDIDATE) {
            tailLength = iterator.next().getPulseList().getPulseLengths().get(0);
        }
        return tailLength;
    }

    private static boolean isSpecifiedPulsePair(long pulseLength, Long pulse1, Long pulse2) {
        return pulse1 == pulseLength && pulse2 == pulseLength;
    }

    private static long getPulseLength(TapeBlock block, BinaryOperator<Long> operator) {
        return block.getRangeAverages().values().stream().reduce(operator).orElse(0L) / 2;
    }

    private static PZXPulseBlock getPzxPulseBlock(PeekingIterator<TapeBlock> iterator, PulseList blockPulseList) {
        if (iterator.hasNext() && iterator.peek().getBlockType() == SYNC_CANDIDATE) {
            // Make a new PulseList with this block and the next one
            blockPulseList = new PulseList(blockPulseList, iterator.next().getPulseList());
        }
        return new PZXPulseBlock(blockPulseList);
    }

}
