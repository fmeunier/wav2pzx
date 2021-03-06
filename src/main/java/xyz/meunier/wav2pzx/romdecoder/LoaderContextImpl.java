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
package xyz.meunier.wav2pzx.romdecoder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import xyz.meunier.wav2pzx.blocks.*;
import xyz.meunier.wav2pzx.databuilder.DataBuilder;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * LoaderContextImpl represents the extrinsic state of the tape processing state
 * machine. The data extracted from the source file will be stored here and
 * @author Fredrick Meunier
 */
public final class LoaderContextImpl implements LoaderContext {
    
    // Holds the current pulses under consideration, will be transformed to a
    // suitable PULS or DATA block in the resulting PZX file
    private List<Long> pulseLengths = new ArrayList<>();
    
    // Holds the results of processing the data input, a series of blocks of 
    // pulses suitable for storing in PZX format blocks
    private final ArrayList<PZXBlock> loaderResult = new ArrayList<>();
    
    // The current binary signal level
    private int currentLevel;

    // Holds all the detected pilot pulses from the source for this block
    private final ArrayList<Long> pilotPulses = new ArrayList<>();

    // Holds all the detected 0 bit pulses from the source
    private final ArrayList<Long> zeroPulses = new ArrayList<>();
    
    // Holds all the detected 1 bit pulses from the source
    private final ArrayList<Long> onePulses = new ArrayList<>();
    
    // Holds the sequence of bytes decoded from the pulse stream
    private DataBuilder dataBuilder = new DataBuilder();
    
    // Length of the SYNC1 pulse found on the tape
    private long sync1Length;
    
    // Length of the SYNC2 pulse found on the tape
    private long sync2Length;
    
    // An iterator over the PulseList, allows peeking for a 1 pulse lookahead
    private final PeekingIterator<Long> pulseIterator;
    
    // The length of the current pulse being processed
    private long currentPulse;
    
    // First pulse level found in the block (0 or 1), used in block construction
    private int firstPulseLevel;
    
    // The data in a ROM block is terminated by an optional pulse of 945 T States,
    // If found that should be recorded here
    private long tailLength;

    // The resolution of the underlying source in units of the target clock rate
	private final long resolution;

    /**
     * Builder method to construct a series of PZXBlocks that represents the data
     * in the supplied PulseList.
     * @param pulseList the tape data to analyse
     * @return the analysed tape image
     * @throws NullPointerException if pulseList was null
     */
    static public List<PZXBlock> buildPZXTapeList(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList was null");
        final LoaderContextImpl context = new LoaderContextImpl(pulseList);
            
        LoaderState state = LoaderState.INITIAL;

        while(context.hasNextPulse()) {
            context.getNextPulse();
            state = state.nextState(context);
        }

        // Terminate the tape
        state.endLoader(context);

        Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, context.toString());
        
        return context.getPZXTapeList();
    }

    @Override
    public long getSync1Length() {
        return sync1Length;
    }

    @Override
    public long getSync2Length() {
        return sync2Length;
    }

    /**
     * Construct a new LoaderContextImpl
     * @param pulseList list of pulses from source tape (cannot be null)
     * @throws NullPointerException if pulseList is null
     */
    LoaderContextImpl(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList cannot be null");
        this.currentLevel = pulseList.getFirstPulseLevel() == 0 ? 1 : 0; // will be inverted when first pulse is retrieved
        this.pulseIterator = Iterators.peekingIterator(pulseList.getPulseLengths().iterator());
        this.resolution = pulseList.getResolution();
        loaderResult.add(new PZXHeaderBlock());
        resetBlock();
    }

    private void addPulse(Long pulseLength) {
    	if(pulseLengths.isEmpty()) {
    		firstPulseLevel = currentLevel;
    	}
    	
    	pulseLengths.add(pulseLength);
    }
    
    /**
     *
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    @Override
    public void addZeroPulse(Long firstPulseLength, Long secondPulseLength) {
        addPulse(firstPulseLength);
        addPulse(secondPulseLength);
        zeroPulses.add(firstPulseLength);
        zeroPulses.add(secondPulseLength);
        dataBuilder.addBit(0);
    }

    @Override
    public void resetBlock() {
        pilotPulses.clear();
        zeroPulses.clear();
        onePulses.clear();

        dataBuilder = new DataBuilder();

        sync1Length = 0;
        sync2Length = 0;
        tailLength = 0;
    }

    @Override
    public void addOnePulse(Long firstPulseLength, Long secondPulseLength) {
        addPulse(firstPulseLength);
        addPulse(secondPulseLength);
        onePulses.add(firstPulseLength);
        onePulses.add(secondPulseLength);
        dataBuilder.addBit(1);
    }

    @Override
    public void completePulseBlock(boolean isPilot) {
        PZXBlock newBlock;
        if(pulseLengths.isEmpty()) {
        	newBlock = new PZXNullBlock();
            loaderResult.add(newBlock);
            Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, newBlock.getSummary());
            return;
        }
        
        if(isPilot) {
            newBlock = new PZXPilotBlock(getPulseListForCurrentPulses());
            LongSummaryStatistics stats = getSummaryStats(pilotPulses);
            Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, getSummaryText("pilot", PILOT_LENGTH, stats));
            // if average PILOT_LENGTH pulse length is not plausibly the same as standard, record this as a non-pilot block
            if(!PulseUtils.equalWithinResolution(PILOT_LENGTH, stats.getAverage(), resolution)) {
	        	completePulseBlock(false);
	            return;
            }
        } else {
            newBlock = new PZXPulseBlock(getPulseListForCurrentPulses());
        }
        Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, newBlock.getSummary());
        loaderResult.add(newBlock);
        pulseLengths.clear();
        resetBlock();
    }

    @Override
    public void addPilotPulse(Long pulseLength) {
        addPulse(pulseLength);
        pilotPulses.add(pulseLength);
    }

    @Override
    public void completeDataBlock() {
        if(pulseLengths.isEmpty()) {
            return;
        }

        ImmutableList<Byte> data = dataBuilder.getData();
        int numBitsInLastByte = dataBuilder.getNumBitsInCurrentByte();

        LongSummaryStatistics zeroStats = getSummaryStats(zeroPulses);
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, getSummaryText("zero", ZERO, zeroStats));
        LongSummaryStatistics oneStats = getSummaryStats (onePulses);
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, getSummaryText("one", ONE, oneStats));

		// TODO: use average ZERO pulse length unless idealised, actually - only create data block if average zero pulse
		// credibly resembles the standard zero pulse, the recognition routines seem to be close to handling standard
		// speed loaders where the standard routines just have shorter timing constants than the standard ROM routines
        if(data.isEmpty() ||
        	(!PulseUtils.equalWithinResolution(ZERO, zeroStats.getAverage(), resolution) && zeroStats.getCount() != 0) ||
        
        // TODO: use average ONE pulse length unless idealised, actually - only create data block if average one pulse
		// credibly resembles the standard one pulse, the recognition routines seem to be close to handling standard
		// speed loaders where the standard routines just have shorter timing constants than the standard ROM routines
            (!PulseUtils.equalWithinResolution(ONE, oneStats.getAverage(), resolution) && oneStats.getCount() != 0)) {
        	// Something was wrong with this block as a data block, try again to record it as a plain pulse block
        	completePulseBlock(false);
        	
            return;
		}

		PZXDataBlock newBlock = 
                new PZXDataBlock(getPulseListForCurrentPulses(), numBitsInLastByte, data);
        
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.INFO, newBlock.getSummary());
        loaderResult.add(newBlock);
        pulseLengths.clear();
        resetBlock();
    }

	private PulseList getPulseListForCurrentPulses() {
		return new PulseList(pulseLengths, firstPulseLevel, resolution);
	}

    @Override
    public void addUnclassifiedPulse(Long pulseLength) {
        addPulse(pulseLength);
    }

    @Override
    public void revertCurrentBlock() {
        // Check loaderResult is not just the header block added by default
    	if(loaderResult.size() < 2) return;
        final int lastIndex = loaderResult.size()-1;
        final PZXBlock lastBlock = loaderResult.remove(lastIndex);
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, "reverting block: " + lastBlock.getSummary() + "\n");
        List<Long> newPulseLengths = new ArrayList<>(lastBlock.getPulses());
        newPulseLengths.addAll(pulseLengths);
        pulseLengths = newPulseLengths;
        firstPulseLevel = lastBlock.getFirstPulseLevel();
        resetBlock();
    }

    @Override
    public int getNumPilotPulses() {
        return pilotPulses.size();
    }

    @Override
    public void addSync1(Long pulseLength) {
        addPulse(pulseLength);
        sync1Length = pulseLength;
    }

    @Override
    public void addSync2(Long pulseLength) {
        addPulse(pulseLength);
        sync2Length = pulseLength;
    }

    @Override
    public void setTailLength(Long pulseLength) {
        addPulse(pulseLength);
        tailLength = pulseLength;
    }
    
    @Override
    public long getTailLength() {
        return tailLength;
    }
    
    /**
     *
     * @param data the value of data
     * @return the java.util.DoubleSummaryStatistics
     */
    private LongSummaryStatistics getSummaryStats(ArrayList<Long> data) {
        return data.stream().mapToLong(x -> x).summaryStatistics();
    }

    /**
     *
     * @param type the value of type
     * @param standardPulse the value of standardPulse
     * @param stats the value of stats
     * @return the String
     */
    private String getSummaryText(String type, int standardPulse, LongSummaryStatistics stats) {
        StringBuilder retval = new StringBuilder();
        long low = stats.getMin();
        long high = stats.getMax();
        double average = stats.getAverage();
        retval.append("shortest ").append(type).append(" pulse:").append(low)
                .append(" tstates, longest ").append(type).append(" pulse:")
                .append(high).append(" tstates\n");
        retval.append("shortest ").append(type).append(" pulse ")
                .append(String.format("%.2f", (double)low/standardPulse*100.0))
                .append("% of expected, longest ").append(type)
                .append(" pulse ").append(String.format("%.2f", (double)high/standardPulse*100.0))
                .append("% of expected\n");
        retval.append("average ").append(type).append(" pulse:").append(String.format("%.2f", average))
                .append("\n");
        return retval.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("LoaderContextImpl{" + "loaderResult=\n");
        loaderResult.stream().forEach((b) -> builder.append('\t').append(b).append('\n'));
        builder.append('}');
        
        return builder.toString();
    }

    @Override
    public boolean hasNextPulse() {
        return this.pulseIterator.hasNext();
    }

    @Override
    public Long peekNextPulse() {
        return this.pulseIterator.peek();
    }

    @Override
    public long getCurrentPulse() {
        return this.currentPulse;
    }

    @Override
    public int getCurrentPulseLevel() {
        return this.currentLevel;
    }

    @Override
    public long getNextPulse() {
        this.currentPulse = this.pulseIterator.next();
        this.currentLevel = this.currentLevel == 0 ? 1 : 0;
        return this.currentPulse;
    }

    /**
     * Get a copy of the current list of identified PZXBlocks 
     * @return the current list of identified PZXBlocks
     */
    List<PZXBlock> getPZXTapeList() {
        return new ArrayList<>(this.loaderResult);
    }
    
    /**
     * Get a copy of the current list of pulses in the block being built
     * @return a copy of the current list of pulses
     */
    public List<Long> getPulseLengths() {
        return new ArrayList<>(pulseLengths);
    }

    /**
     * Get a copy of the current list of zero bit pulses in the block being built
     * @return a copy of the current list of zero bit pulses
     */
    List<Long> getZeroPulses() {
        return new ArrayList<>(zeroPulses);
    }

    /**
     * Get a copy of the current list of one bit pulses in the block being built
     * @return a copy of the current list of zero bit pulses
     */
    List<Long> getOnePulses() {
        return new ArrayList<>(onePulses);
    }

    /**
     * Get a copy of the current list of pilot pulses in the block being built
     * @return a copy of the current list of pilot pulses
     */
    List<Long> getPilotPulses() {
        return new ArrayList<>(pilotPulses);
    }
    
	@Override
	public long getResolution() {
		return this.resolution;
	}    
    
}
