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
package xyz.meunier.wav2pzx;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * LoaderContextImpl represents the extrinsic state of the tape processing state
 * machine. The data extracted from the source file will be stored here and
 * @author Fredrick Meunier
 */
public final class LoaderContextImpl implements LoaderContext {
    
    // Holds the current pulses under consideration, will be transformed to a
    // suitable PULS or DATA block in the resulting PZX file
    private List<Double> pulseLengths = new ArrayList<>();
    
    // Holds the results of processing the data input, a series of blocks of 
    // pulses suitable for storing in PZX format blocks
    private final ArrayList<PZXBlock> loaderResult = new ArrayList<>();
    
    // The current binary signal level
    private int currentLevel;
    
    // Holds all the detected pilot pulses from the source for thi block
    private final ArrayList<Double> pilotPulses = new ArrayList<>();
    
    // Holds all the detected 0 bit pulses from the source
    private final ArrayList<Double> zeroPulses = new ArrayList<>();
    
    // Holds all the detected 1 bit pulses from the source
    private final ArrayList<Double> onePulses = new ArrayList<>();
    
    // Holds the sequence of bytes decoded from the pulse stream
    private final ArrayList<Byte> data = new ArrayList<>();

    // Holds the in-progress byte being built from the tape bitstream
    private byte currentByte;
    // Number of bits received so far for the currentByte
    private int numBitsInCurrentByte;
    
    // Length of the SYNC1 pulse found on the tape
    private double sync1Length;
    
    // Length of the SYNC2 pulse found on the tape
    private double sync2Length;
    
    // An iterator over the PulseList, allows peeking for a 1 pulse lookahead
    private final PeekingIterator<Double> pulseIterator;
    
    // The length of the current pulse being processed
    private double currentPulse;
    
    // First pulse level found in the block (0 or 1), used in block construction
    private int firstPulseLevel;
    
    // The data in a ROM block is terminated by an optional pulse of 945 T States,
    // If found that should be recorded here
    private double tailLength;

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
    public double getSync1Length() {
        return sync1Length;
    }

    @Override
    public double getSync2Length() {
        return sync2Length;
    }

    /**
     * Construct a new LoaderContextImpl
     * @param pulseList list of pulses from source tape (cannot be null)
     * @throws NullPointerException if pulseList is null
     */
    public LoaderContextImpl(PulseList pulseList) {
        checkNotNull(pulseList, "pulseList cannot be null");
        this.currentLevel = pulseList.getFirstPulseLevel() == 0 ? 1 : 0; // will be inverted when first pulse is retrieved
        this.pulseIterator = Iterators.peekingIterator(pulseList.getPulseLengths().iterator());
        loaderResult.add(new PZXHeaderBlock());
        resetBlock();
    }

    private void addPulse(Double pulseLength) {
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
    public void addZeroPulse(Double firstPulseLength, Double secondPulseLength) {
        addPulse(firstPulseLength);
        addPulse(secondPulseLength);
        zeroPulses.add(firstPulseLength);
        zeroPulses.add(secondPulseLength);
        addBit( 0 );
    }

    @Override
    public void addBit(int bit) {
        currentByte <<= 1;
        currentByte |= (bit & 0x01);
        if( ++numBitsInCurrentByte == 8 ) {
            data.add(currentByte);
            currentByte = 0;
            numBitsInCurrentByte = 0;
        }
    }

    @Override
    public void resetBlock() {
        pilotPulses.clear();
        zeroPulses.clear();
        onePulses.clear();
        data.clear();
        
        currentByte = 0;
        numBitsInCurrentByte = 0;
        sync1Length = 0;
        sync2Length = 0;
        tailLength = 0;
    }

    @Override
    public void addOnePulse(Double firstPulseLength, Double secondPulseLength) {
        addPulse(firstPulseLength);
        addPulse(secondPulseLength);
        onePulses.add(firstPulseLength);
        onePulses.add(secondPulseLength);
        addBit( 1 );
    }

    @Override
    public void completePulseBlock(boolean isPilot) {
        if(pulseLengths.isEmpty()) {
            return;
        }
        
        PZXBlock newBlock;
        if(isPilot) {
            newBlock = new PZXPilotBlock(getPulseListForCurrentPulses());
            DoubleSummaryStatistics stats = getSummaryStats (pilotPulses);
            Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, getSummaryText("pilot", PILOT_LENGTH, stats));
            // TODO: use average PILOT_LENGTH pulse length unless idealised length should be used
        } else {
            newBlock = new PZXPulseBlock(getPulseListForCurrentPulses());
        }
        Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, newBlock.getSummary());
        loaderResult.add(newBlock);
        pulseLengths.clear();
        resetBlock();
    }

    @Override
    public void addPilotPulse(Double pulseLength) {
        addPulse(pulseLength);
        pilotPulses.add(pulseLength);
    }

    @Override
    public void completeDataBlock() {
        if(pulseLengths.isEmpty()) {
            return;
        }

        // add any partially accumulated byte to the data collection before
        // considering this block complete
        if( numBitsInCurrentByte != 0 ) {
            data.add(currentByte);
        }
        
        int numBitsInLastByte = numBitsInCurrentByte == 0  ? 8 : numBitsInCurrentByte;
        
		PZXDataBlock newBlock = 
                new PZXDataBlock(getPulseListForCurrentPulses(), tailLength, 
                				 numBitsInLastByte, data);
        
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, newBlock.getSummary());
        loaderResult.add(newBlock);
        pulseLengths.clear();

        DoubleSummaryStatistics stats = getSummaryStats(zeroPulses);
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, getSummaryText("zero", ZERO, stats));
        // TODO: use average ZERO pulse length unless idealised
        
        stats = getSummaryStats (onePulses);
		Logger.getLogger(LoaderContextImpl.class.getName()).log(Level.FINE, getSummaryText("one", ONE, stats));
        // TODO: use average ONE pulse length unless idealised
        
        resetBlock();
    }

	private PulseList getPulseListForCurrentPulses() {
		return new PulseList(pulseLengths, firstPulseLevel);
	}

    @Override
    public void addUnclassifiedPulse(Double pulseLength) {
        addPulse(pulseLength);
    }

    @Override
    public void revertCurrentBlock() {
        // Check loaderResult is not empty
        checkState(!loaderResult.isEmpty());
        final int lastIndex = loaderResult.size()-1;
        final PZXBlock lastBlock = loaderResult.remove(lastIndex);
        List<Double> newPulseLengths = new ArrayList<>(lastBlock.getPulses());
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
    public void addSync1(Double pulseLength) {
        addPulse(pulseLength);
        sync1Length = pulseLength;
    }

    @Override
    public void addSync2(Double pulseLength) {
        addPulse(pulseLength);
        sync2Length = pulseLength;
    }

    @Override
    public void setTailLength(Double pulseLength) {
        addPulse(pulseLength);
        tailLength = pulseLength;
    }
    
    @Override
    public double getTailLength() {
        return tailLength;
    }
    
    /**
     *
     * @param data the value of data
     * @return the java.util.DoubleSummaryStatistics
     */
    private DoubleSummaryStatistics getSummaryStats(ArrayList<Double> data) {
        return data.stream().mapToDouble(x -> x).summaryStatistics();
    }

    /**
     *
     * @param type the value of type
     * @param standardPulse the value of standardPulse
     * @param stats the value of stats
     * @return the String
     */
    private String getSummaryText(String type, int standardPulse, DoubleSummaryStatistics stats) {
        StringBuilder retval = new StringBuilder();
        double low = stats.getMin();
        double high = stats.getMax();
        double average = stats.getAverage();
        retval.append("shortest ").append(type).append(" pulse:").append(low)
                .append(" tstates, longest ").append(type).append(" pulse:")
                .append(high).append(" tstates\n");
        retval.append("shortest ").append(type).append(" pulse ")
                .append((double)low/standardPulse*100)
                .append("% of expected, longest ").append(type)
                .append(" pulse ").append((double)high/standardPulse*100)
                .append("% of expected\n");
        retval.append("average ").append(type).append(" pulse:").append(average)
                .append("\n");
        return retval.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("LoaderContextImpl{" + "loaderResult=\n");
        loaderResult.stream().forEach((b) -> {
            builder.append('\t').append(b).append('\n');
        });
        builder.append('}');
        
        return builder.toString();
    }

    @Override
    public boolean hasNextPulse() {
        return this.pulseIterator.hasNext();
    }

    @Override
    public Double peekNextPulse() {
        return this.pulseIterator.peek();
    }

    @Override
    public double getCurrentPulse() {
        return this.currentPulse;
    }

    @Override
    public int getCurrentPulseLevel() {
        return this.currentLevel;
    }

    @Override
    public double getNextPulse() {
        this.currentPulse = this.pulseIterator.next();
        this.currentLevel = this.currentLevel == 0 ? 1 : 0;
        return this.currentPulse;
    }

    /**
     * Get a copy of the current list of identified PZXBlocks 
     * @return the current list of identified PZXBlocks
     */
    public List<PZXBlock> getPZXTapeList() {
        return new ArrayList<>(this.loaderResult);
    }
    
    /**
     * Get a copy of the current list of pulses in the block being built
     * @return a copy of the current list of pulses
     */
    public List<Double> getPulseLengths() {
        return new ArrayList<>(pulseLengths);
    }

    /**
     * Get a copy of the current list of zero bit pulses in the block being built
     * @return a copy of the current list of zero bit pulses
     */
    public List<Double> getZeroPulses() {
        return new ArrayList<>(zeroPulses);
    }

    /**
     * Get the in-progress byte being constructed from the decoded bits
     * @return the in-progress byte being built
     */
    public byte getCurrentByte() {
        return currentByte;
    }
    
    /**
     * Get the number of bits in the in-progress byte being constructed from the decoded bits
     * @return the number of bits in the in-progress byte being built
     */
    public int getNumBitsInCurrentByte() {
    	return numBitsInCurrentByte;
    }

    /**
     * Get a copy of the current list of one bit pulses in the block being built
     * @return a copy of the current list of zero bit pulses
     */
    public List<Double> getOnePulses() {
        return new ArrayList<>(onePulses);
    }

    /**
     * Get a copy of the current list of pilot pulses in the block being built
     * @return a copy of the current list of pilot pulses
     */
    public List<Double> getPilotPulses() {
        return new ArrayList<>(pilotPulses);
    }
    
    /**
     * Get a copy of the current list of data bytes in the block being built
     * @return a copy of the current list of data bytes
     */
    public List<Byte> getData() {
    	return new ArrayList<>(data);
    }
    
    
}
