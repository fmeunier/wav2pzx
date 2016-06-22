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

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.empty;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

/**
 * Manages the construction of a list of TapeBlocks. Any null or optional not present blocks supplied are omitted from
 * the final built list.
 */
final class TapeBlockListBuilder {

    private Deque<Optional<TapeBlock>> tapeBlocks = new LinkedList<>();

    /**
     * Add the non-null blocks from the supplied collection to the list under construction.
     * @param newBlocks the list of blocks to add
     * @throws NullPointerException if the supplied newBlocks is null
     */
    void addAll(Collection<Optional<TapeBlock>> newBlocks) {
        checkNotNull(newBlocks, "newBlocks cannot be null");
        newBlocks.stream().filter(Objects::nonNull).forEach(tapeBlocks::add);
    }

    /**
     * Add a block to the list under construction
     * @param newBlock the block to be added
     */
    void add(TapeBlock newBlock) {
        Optional<TapeBlock> newEntry = newBlock == null ? empty() : Optional.of(newBlock);
        getLogger(TapeBlockListBuilder.class.getName()).log(Level.FINE, newEntry.toString());
        tapeBlocks.add(newEntry);
    }

    /**
     * Returns the last added TapeBlock from the list without modifying the list being built.
     * @return the last added TapeBlock
     */
    Optional<TapeBlock> peekLastBlock() {
        return getTapeBlock(() -> tapeBlocks.peekLast());
    }

    /**
     * Removes the last added TapeBlock from the list being built and returns it.
     * @return the last added TapeBlock
     */
    Optional<TapeBlock> removeLastBlock() {
        return getTapeBlock(() -> tapeBlocks.removeLast());
    }

    /**
     * Builds the list of TapeBlocks
     * @return the list of present/non-null TapeBlocks that have been provided
     */
    List<TapeBlock> build() {
        return tapeBlocks.stream().filter(Optional::isPresent).map(Optional::get).collect(toList());
    }

    private Optional<TapeBlock> getTapeBlock(Supplier<Optional<TapeBlock>> supplier) {
        return tapeBlocks.isEmpty() ? empty() : supplier.get();
    }

    Optional<TapeBlock> removeLastBlockIfTrue(Predicate<TapeBlock> predicate) {
        checkNotNull(predicate, "predicate cannot be null");

        Optional<TapeBlock> lastBlock = peekLastBlock();
        if (!lastBlock.isPresent() || !predicate.test(lastBlock.get())) return empty();
        return removeLastBlock();
    }

}
