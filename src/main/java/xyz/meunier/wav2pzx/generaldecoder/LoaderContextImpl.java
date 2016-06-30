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
import javafx.util.Pair;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;
import static xyz.meunier.wav2pzx.generaldecoder.LoaderState.INITIAL;

/**
 * LoaderContextImpl represents the extrinsic state of the tape processing state
 * machine. The data extracted from the source file will be stored here and
 *
 * @author Fredrick Meunier
 */
class LoaderContextImpl implements LoaderContext {

    private final long resolution;
    private int nextIndex;
    private int firstIndexOfBlock;
    private int lastIndexOfBlock;

    private int currentLevel;

    private TapeBlockListBuilder tapeBlockListBuilder = new TapeBlockListBuilder();

    private long currentPulse;
    private int pilotPulseCount;
    private ImmutableList<Long> pulseLengths;
    private int firstPulseLevel;

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

    void getNextPulse() {
        currentPulse = pulseLengths.get(nextIndex++);
        currentLevel = invertPulseLevel(currentLevel);
    }

    private boolean hasNextPulse() {
        return nextIndex < pulseLengths.size();
    }

    @Override
    public long getResolution() {
        return resolution;
    }

    @Override
    public void completeDataBlock() {
        completeBlock(BlockType.DATA);
    }

    private PulseList buildPulseList() {
        return new PulseList(pulseLengths.subList(firstIndexOfBlock, lastIndexOfBlock), firstPulseLevel, resolution);
    }

    @Override
    public void completeUnknownPulseBlock() {
        completeBlock(BlockType.UNKNOWN);
    }

    @Override
    public void completePilotPulseBlock() {
        completeBlock(BlockType.PILOT);
    }

    private void completeBlock(BlockType blockType) {
        if (firstIndexOfBlock >= lastIndexOfBlock) {
            tapeBlockListBuilder.add(null);
            return;
        }
        tapeBlockListBuilder.add(new Pair<>(blockType, buildPulseList()));
        firstIndexOfBlock = lastIndexOfBlock;
        resetBlock();
    }

    @Override
    public void addPilotPulse() {
        lastIndexOfBlock++;
        pilotPulseCount++;
    }

    @Override
    public void addUnclassifiedPulse() {
        lastIndexOfBlock++;
    }

    @Override
    public void revertCurrentBlock() {
        final Optional<Pair<BlockType, PulseList>> lastTapeBlock = tapeBlockListBuilder.removeLastBlock();

        getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, "reverting block: " + lastTapeBlock.toString() + "\n");

        // In this case, popping off the last not present block will have done the job
        if (!lastTapeBlock.isPresent()) return;

        PulseList lastBlock = lastTapeBlock.get().getValue();
        // Preserve any pulses from the last block
        firstIndexOfBlock -= lastBlock.getPulseLengths().size();

        // If the last block was null this block has the same first pulse level that would have had
        resetBlock(lastBlock.getFirstPulseLevel()); // FIXME: Will have lost pilot count if it was present in the last block
    }

    @Override
    public int getNumPilotPulses() {
        return pilotPulseCount;
    }

    @Override
    public boolean isaPilotCandidate() {
        return LoaderContext.isaPilotCandidate(currentPulse);
    }

    @Override
    public boolean isTooLongToBeAPilot() {
        return currentPulse > MAX_PILOT_LENGTH;
    }

    @Override
    public boolean isaDataCandidate() {
        return currentPulse < MIN_PILOT_LENGTH;
    }

    @Override
    public boolean isaCandidateTailPulse() {
        return currentPulse <= LoaderContext.MAX_TAIL_PULSE;
    }

    @Override
    public boolean isCurrentAndNextPulseTooLongToBeADataCandidate() {
        // Any two adjacent data pulses must be less than DATA_TOTAL_MAX
        long nextPulse = hasNextPulse() ? pulseLengths.get(this.nextIndex) : 0L;
        return (currentPulse + nextPulse) > DATA_TOTAL_MAX;
    }

    @Override
    public boolean isaMinimumNumberOfPilotPulses() {
        return pilotPulseCount >= MIN_PILOT_COUNT;
    }

    LoaderContextImpl(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList cannot be null");
        this.currentLevel = invertPulseLevel(pulseList.getFirstPulseLevel()); // will be inverted when first pulse is retrieved
        this.resolution = pulseList.getResolution();
        pulseLengths = pulseList.getPulseLengths();
        nextIndex = 0;
        firstIndexOfBlock = 0;
        lastIndexOfBlock = 0;
        resetBlock(pulseList.getFirstPulseLevel());
    }

    List<TapeBlock> getTapeBlockList() {
        return tapeBlockListBuilder.build();
    }

    private int invertPulseLevel(int level) {
        return level == 0 ? 1 : 0;
    }

    private void resetBlock() {
        resetBlock(currentLevel);
    }

    private void resetBlock(int firstPulseLevel) {
        this.firstPulseLevel = firstPulseLevel;
        pilotPulseCount = 0;
    }

    @Override
    public String toString() {
        return "LoaderContextImpl{" +
                "resolution=" + resolution +
                ", nextIndex=" + nextIndex +
                ", firstIndexOfBlock=" + firstIndexOfBlock +
                ", lastIndexOfBlock=" + lastIndexOfBlock +
                ", currentLevel=" + currentLevel +
                ", tapeBlockListBuilder=" + tapeBlockListBuilder +
                ", currentPulse=" + currentPulse +
                ", pilotPulseCount=" + pilotPulseCount +
                ", firstPulseLevel=" + firstPulseLevel +
                '}';
    }
}
