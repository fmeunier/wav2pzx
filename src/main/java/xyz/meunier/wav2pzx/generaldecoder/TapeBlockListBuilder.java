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

import com.google.common.collect.PeekingIterator;
import javafx.util.Pair;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.google.common.collect.Iterators.peekingIterator;
import static java.util.Optional.empty;
import static java.util.logging.Logger.getLogger;
import static xyz.meunier.wav2pzx.generaldecoder.BlockType.DATA;
import static xyz.meunier.wav2pzx.generaldecoder.HeaderPulseProcessor.processPulseBlock;
import static xyz.meunier.wav2pzx.generaldecoder.LoaderContext.isaPilotCandidate;

/**
 * Manages the construction of a list of TapeBlocks. Any null or optional not present blocks supplied are omitted from
 * the final built list.
 */
final class TapeBlockListBuilder {

    private Deque<Optional<Pair<BlockType, PulseList>>> tapeBlocks = new LinkedList<>();

    /**
     * Add a block to the list under construction
     *
     * @param newBlock the block to be added
     */
    void add(Pair<BlockType, PulseList> newBlock) {
        Optional<Pair<BlockType, PulseList>> newEntry = newBlock == null ? empty() : Optional.of(newBlock);
        getLogger(TapeBlockListBuilder.class.getName()).log(Level.FINE, newEntry.toString());
        tapeBlocks.add(newEntry);
    }

    /**
     * Returns the last added TapeBlock from the list without modifying the list being built.
     *
     * @return the last added TapeBlock
     */
    Optional<Pair<BlockType, PulseList>> peekLastBlock() {
        return getTapeBlock(() -> tapeBlocks.peekLast());
    }

    /**
     * Removes the last added TapeBlock from the list being built and returns it.
     *
     * @return the last added TapeBlock
     */
    Optional<Pair<BlockType, PulseList>> removeLastBlock() {
        return getTapeBlock(() -> tapeBlocks.removeLast());
    }

    /**
     * Builds the list of TapeBlocks
     *
     * @return the list of present/non-null TapeBlocks that have been provided
     */
    List<TapeBlock> build() {
        List<TapeBlock> tapeBlockList = new ArrayList<>();
        PeekingIterator<Optional<Pair<BlockType, PulseList>>> iterator = peekingIterator(tapeBlocks.iterator());
        while (iterator.hasNext()) {
            iterator.next().ifPresent(pair -> {
                if (pair.getKey() == DATA)
                    tapeBlockList.addAll(getDataBlocks(pair.getValue()));
                else
                    tapeBlockList.add(getPilotBlock(pair.getValue(), iterator));
            });
        }

        return tapeBlockList;
    }

    private Collection<? extends TapeBlock> getDataBlocks(PulseList pulseList) {
        return new DualPulseDataBlockProcessor(pulseList).processDataBlock();
    }

    private TapeBlock getPilotBlock(PulseList thisBlockPulses, PeekingIterator<Optional<Pair<BlockType, PulseList>>> iterator) {
        // For unknown, check the next block for being a pilot if it is a one pulse pilot candidate to merge with this
        // block as the data block processor can leave an additional block prior to the main pilot block that has some
        // of the associated pilot pulses
        PulseList newBlockPulses = thisBlockPulses;
        if (thisBlockPulses.getPulseLengths().size() == 1 &&
                isaPilotCandidate(thisBlockPulses.getPulseLengths().get(0)) &&
                isaPilotBlockNext(iterator)) {
            newBlockPulses =
                    new PulseList(thisBlockPulses, iterator.next().orElseThrow(NullPointerException::new).getValue());
        }
        return processPulseBlock(newBlockPulses);
    }

    private boolean isaPilotBlockNext(PeekingIterator<Optional<Pair<BlockType, PulseList>>> iterator) {
        return iterator.hasNext() && isaFollowingPilotBlock(iterator);
    }

    private boolean isaFollowingPilotBlock(PeekingIterator<Optional<Pair<BlockType, PulseList>>> iterator) {
        Optional<Pair<BlockType, PulseList>> nextPair = iterator.peek();
        return nextPair.isPresent() && nextPair.get().getKey() == BlockType.PILOT;
    }

    private Optional<Pair<BlockType, PulseList>> getTapeBlock(Supplier<Optional<Pair<BlockType, PulseList>>> supplier) {
        return tapeBlocks.isEmpty() ? empty() : supplier.get();
    }

}
