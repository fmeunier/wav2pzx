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
    int DATA_TOTAL_MAX = 3950; // TODO: check this - loading two edges with a timing constant of 0xb0 for a bit implies 4450 as the total?
    
    /**
     * The maximum length of a tail pulse
     */
    int MAX_TAIL_PULSE = 1700;
    
    /**
     * The minimum number of pilot pulses before a sync1 pulse can occur
     */
    int MIN_PILOT_COUNT = 256;
    
    /**
     * The standard duration of a "one" bit pulse
     */
    short ONE = 1710;
    
    /**
     * The standard length of a pilot pulse
     */
    int PILOT_LENGTH = 2168;
    
    /**
     * The maximum valid length of a pilot pulse
     */
    int PILOT_MAX = 3000;

    /**
     * The standard length of a sync1 pulse
     */
    int SYNC1 = 667;
    
    /**
     * The maximum length of a sync1 pulse
     */
    int SYNC1_MAX = 1100;
    
    /**
     * The standard length of a sync2 pulse
     */
    int SYNC2 = 735;
    
    /**
     * The maximum length the two sync pulses can be
     */
    int SYNC_TOTAL_MAX = 3594;
    
    /**
     * The standard length of a "zero" bit pulse
     */
    short ZERO = 855;
    
    /**
     * The threshold between two pulses being a zero or one bit if they are less than DATA_TOTAL_MAX
     */
    int ZERO_THRESHOLD = 2400;
    
    /**
     * The standard length of a tail pulse at the end of a data block
     */
    short TAIL = 945;

    /**
     * Add the two "one" pulses that have been identified as a "one" bit to the
     * data stream
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    void addOnePulse(Long firstPulseLength, Long secondPulseLength);

    /**
     * Add an identified pilot pulse to the block in progress.
     * @param pulseLength the pilot pulse
     */
    void addPilotPulse(Long pulseLength);

    /**
     * Add an identified sync1 pulse to the block in progress
     * @param pulseLength the sync1 pulse
     */
    void addSync1(Long pulseLength);

    /**
     * Add an identified sync2 pulse to the block in progress
     * @param pulseLength the sync2 pulse
     */
    void addSync2(Long pulseLength);

    /**
     * Add an unclassified pulse to the current block in progress
     * @param pulseLength the unclassified pulse
     */
    void addUnclassifiedPulse(Long pulseLength);

    /**
     * Add the two "zero" pulses that have been identified as a "zero" bit to the
     * data stream
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    void addZeroPulse(Long firstPulseLength, Long secondPulseLength);

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
    long getSync1Length();

    /**
     * @return the duration of the sync2 pulse in the current block
     */
    long getSync2Length();

    /**
     * @return the duration of the tail pulse in the current block
     */
    long getTailLength();

    /**
     * Reset the state of the current block to the initial state
     */
    void resetBlock();

    /**
     * If a last block completed was being provisionally treated as a certain 
     * type but subsequent data shows that is not correct, this method removes 
     * the last classified block and adds its pulses to the beginning of the
     * pulses from the current block in progress. If no pulse blocks have been
     * added, has no effect.
     */
    void revertCurrentBlock();

    /**
     * @return the length of the next pulse in the tape
     * @throws NoSuchElementException if there are no more pulses
     */
    Long peekNextPulse();
    
    /**
     * @return true if there is another pulse to process after the current one
     */
    boolean hasNextPulse();
    
    /**
     * @return the current pulse being processed
     */
    long getCurrentPulse();
    
    /**
     * @return the level (0 or 1) of the current pulse
     */
    int getCurrentPulseLevel();
    
    /**
     * @return the next pulse to be processed
     */
    long getNextPulse();
            
    /**
     * Add an identified tail pulse to the block in progress
     * @param pulseLength the tail pulse
     */
    void setTailLength(Long pulseLength);
    
    /**
     * Get the resolution of each pulse in T-states (error is up to 2 samples)
     * @return the resolution of each pulse in T-states (error is up to 2 samples)
     */
    long getResolution();
}
