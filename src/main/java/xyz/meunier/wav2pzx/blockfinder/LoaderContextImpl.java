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

import com.google.common.collect.PeekingIterator;
import xyz.meunier.wav2pzx.PulseList;
import xyz.meunier.wav2pzx.PulseListBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterators.peekingIterator;
import static java.util.Collections.singletonList;
import static java.util.logging.Logger.getLogger;
import static xyz.meunier.wav2pzx.blockfinder.HeaderPulseProcessor.processPulseBlock;
import static xyz.meunier.wav2pzx.blockfinder.LoaderState.INITIAL;

/**
 * LoaderContextImpl represents the extrinsic state of the tape processing state
 * machine. The data extracted from the source file will be stored here and
 * @author Fredrick Meunier
 */
class LoaderContextImpl implements LoaderContext {

    private static final IsAOnePilotPulseCandidate IS_A_ONE_PILOT_PULSE_CANDIDATE = new IsAOnePilotPulseCandidate();

    private final long resolution;
    // Holds the current pulses under consideration, will be transformed to a
    // suitable PulseList representing the blocks
    private PulseListBuilder builder;

    private int currentLevel;
    private final PeekingIterator<Long> pulseIterator;

    private TapeBlockListBuilder tapeBlockListBuilder = new TapeBlockListBuilder();

    private long currentPulse;
    private int pilotPulseCount;

    /**
     * Builder method to construct a series of PZXBlocks that represents the data
     * in the supplied PulseList.
     *
     * @param pulseList the tape data to analyse
     * @return the analysed tape image
     * @throws NullPointerException if pulseList was null
     */
    static List<TapeBlock> buildTapeBlockList(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList was null");
        final LoaderContextImpl context = new LoaderContextImpl(pulseList);

        LoaderState state = INITIAL;

        while (context.hasNextPulse()) {
            context.getNextPulse();
            state = state.nextState(context);
        }

        // Terminate the tape
        state.endLoader(context);

        getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, context.toString());

        return context.getTapeBlockList();
    }

    @Override
    public long getNextPulse() {
        this.currentPulse = this.pulseIterator.next();
        this.currentLevel = invertPulseLevel(this.currentLevel);
        return this.currentPulse;
    }

    @Override
    public boolean hasNextPulse() {
        return this.pulseIterator.hasNext();
    }

    @Override
    public long getCurrentPulse() {
        return this.currentPulse;
    }

    @Override
    public Long peekNextPulse() {
        return this.pulseIterator.peek();
    }

    @Override
    public long getResolution() {
        return this.builder.getResolution();
    }

    @Override
    public void completeDataBlock() {
        completeBlock(() -> new DualPulseDataBlockProcessor(builder.build()).processDataBlock());
    }

    @Override
    public void completeUnknownPulseBlock() {
        completePulseBlock(() -> builder.build());
    }

    @Override
    public void completePilotPulseBlock() {
        // For pilots check the previous block for being a one pulse pilot candidate to merge with this block as the
        // data block processor can leave an additional block prior to the main pilot block that has some of the
        // associated pilot pulses
        completePulseBlock(() -> {
            Optional<TapeBlock> block = tapeBlockListBuilder.removeLastBlockIfTrue(IS_A_ONE_PILOT_PULSE_CANDIDATE);
            PulseList pulseList = builder.build();
            if(block.isPresent()) {
                pulseList = new PulseList(block.get().getPulseList(), pulseList);
            }
            return pulseList;
        });
    }

    private void completePulseBlock(Supplier<PulseList> supplier) {
        completeBlock(() -> singletonList(Optional.of(processPulseBlock(supplier.get()))));
    }

    private void completeBlock(Supplier<List<Optional<TapeBlock>>> supplier) {
        if (builder.isEmpty()) {
            tapeBlockListBuilder.add(null);
            return;
        }
        tapeBlockListBuilder.addAll(supplier.get());
        resetBlock();
    }

    @Override
    public void addPilotPulse(Long pulseLength) {
        builder.withNextPulse(pulseLength);
        pilotPulseCount++;
    }

    @Override
    public void addUnclassifiedPulse(Long pulseLength) {
        builder.withNextPulse(pulseLength);
    }

    @Override
    public void revertCurrentBlock() {
        final Optional<TapeBlock> lastTapeBlock = tapeBlockListBuilder.removeLastBlock();

        getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, "reverting block: " + lastTapeBlock.toString() + "\n");

        // In this case, popping off the last not present block will have done the job
        if (!lastTapeBlock.isPresent()) return;

        PulseList lastBlock = lastTapeBlock.get().getPulseList();
        PulseList currentBlock = builder.build();

        // If the last block was null this block has the same first pulse level that would have had
        resetBlock(lastBlock.getFirstPulseLevel()); // FIXME: Will have lost pilot count if it was present in the last block

        // Preserve any pulses from the last block
        builder.withNextPulses(lastBlock.getPulseLengths())
                .withNextPulses(currentBlock.getPulseLengths());
    }

    @Override
    public int getNumPilotPulses() {
        return pilotPulseCount;
    }

    LoaderContextImpl(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList cannot be null");
        this.currentLevel = invertPulseLevel(pulseList.getFirstPulseLevel()); // will be inverted when first pulse is retrieved
        this.pulseIterator = peekingIterator(pulseList.getPulseLengths().iterator());
        this.resolution = pulseList.getResolution();
        resetBlock(pulseList.getFirstPulseLevel());
    }

    List<TapeBlock> getTapeBlockList() {
        return tapeBlockListBuilder.build();
    }

    private int invertPulseLevel(int level) {
        return level == 0 ? 1 : 0;
    }

    private void resetBlock() {
        resetBlock(invertPulseLevel(currentLevel));
    }

    private void resetBlock(int firstPulseLevel) {
        builder = new PulseListBuilder().withResolution(resolution).withFirstPulseLevel(firstPulseLevel);
        pilotPulseCount = 0;
    }

}
