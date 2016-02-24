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

import java.util.NoSuchElementException;

/**
 * Base interface for managing the extrinsic state of the tape being analysed.
 * @author Fredrick Meunier
 */
public interface LoaderContext {

    // Constants for Sinclair pulse lengths and limits, from format documentation
    // or analysis of the Sinclair ROM
    
    /**
     * The longest valid length of two pulses to be considered as a bit in the data block
     */
    final int DATA_TOTAL_MAX = 5044;
    
    /**
     * The maximum length of a tail pulse
     */
    final int MAX_TAIL_PULSE = 1050;
    
    /**
     * The minimum number of pilot pulses before a sync1 pulse can occur
     */
    final int MIN_PILOT_COUNT = 256;
    
    /**
     * The standard duration of a "one" bit pulse
     */
    final short ONE = 1710;
    
    /**
     * The standard length of a pilot pulse
     */
    final int PILOT_LENGTH = 2168;
    
    /**
     * The maximim valid length of a pilot pulse
     */
    final int PILOT_MAX = 3000;
    
    /**
     * The standard length of a sync1 pulse
     */
    final int SYNC1 = 667;
    
    /**
     * The maximum length of a sync1 pulse
     */
    final int SYNC1_MAX = 1100;
    
    /**
     * The standard length of a sync2 pulse
     */
    final int SYNC2 = 735;
    
    /**
     * The maximum length the two sync pulses can be
     */
    final int SYNC_TOTAL_MAX = 3594;
    
    /**
     * The standard length of a "zero" bit pulse
     */
    final short ZERO = 855;
    
    /**
     * The threshold between two pulses being a zero or one bit if they are less than DATA_TOTAL_MAX
     */
    final int ZERO_THRESHOLD = 2400;
    
    /**
     * The standard length of a tail pulse at the end of a data block
     */
    final short TAIL = 945;

    /**
     * A data bit has been found in the input stream, accumulate it into the current
     * byte being assembled and when complete, add it to the data collection
     * @param bit 
     */
    void addBit(int bit);

    /**
     * Add the two "one" pulses that have been identified as a "one" bit to the
     * data stream
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    void addOnePulse(Double firstPulseLength, Double secondPulseLength);

    /**
     * Add an identified pilot pulse to the block in progress.
     * @param pulseLength the pilot pulse
     */
    void addPilotPulse(Double pulseLength);

    /**
     * Add an identified sync1 pulse to the block in progress
     * @param pulseLength the sync1 pulse
     */
    void addSync1(Double pulseLength);

    /**
     * Add an identified sync2 pulse to the block in progress
     * @param pulseLength the sync2 pulse
     */
    void addSync2(Double pulseLength);

    /**
     * Add an unclassified pulse to the current block in progress
     * @param pulseLength the unclassified pulse
     */
    void addUnclassifiedPulse(Double pulseLength);

    /**
     * Add the two "zero" pulses that have been identified as a "zero" bit to the
     * data stream
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    void addZeroPulse(Double firstPulseLength, Double secondPulseLength);

    /**
     * All the required components of a data block have been processed, so complete
     * the block and add the completed block to the tape in progress
     */
    void completeDataBlock();

    /**
     * All the required components of a pulse block have been processed, so complete
     * the block and add the completed block to the tape in progress. 
     * @param isPilot this block was a pilot block
     */
    void completePulseBlock(boolean isPilot);

    /**
     * @return the number of pilot pulses found in the block to date
     */
    int getNumPilotPulses();

    /**
     * @return the duration of the sync1 pulse in the current block
     */
    double getSync1Length();

    /**
     * @return the duration of the sync2 pulse in the current block
     */
    double getSync2Length();

    /**
     * @return the duration of the tail pulse in the current block
     */
    double getTailLength();

    /**
     * Reset the state of the current block to the initial state
     */
    void resetBlock();

    /**
     * If a last block completed was being provisionally treated as a certain 
     * type but subsequent data shows that is not correct, this method removes 
     * the last classified block and adds its pulses to the beginning of the
     * pulses from the current block in progress.
     */
    void revertCurrentBlock();

    /**
     * @return the length of the next pulse in the tape
     * @throws NoSuchElementException if there are no more pulses
     */
    Double peekNextPulse();
    
    /**
     * @return true if there is another pulse to process after the current one
     */
    boolean hasNextPulse();
    
    /**
     * @return the current pulse being processed
     */
    double getCurrentPulse();
    
    /**
     * @return the level (0 or 1) of the current pulse
     */
    int getCurrentPulseLevel();
    
    /**
     * @return the next pulse to be processed
     */
    double getNextPulse();
            
    /**
     * Add an identified tail pulse to the block in progress
     * @param pulseLength the tail pulse
     */
    void setTailLength(Double pulseLength);
}
