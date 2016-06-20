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

import com.google.common.collect.Range;

import java.util.NoSuchElementException;

/**
 * Base interface for managing the extrinsic state of the tape being analysed.
 * @author Fredrick Meunier
 */
public interface LoaderContext {

    /**
     * The standard length of a pilot pulse
     */
    int PILOT_LENGTH = 2168;

    /**
     * The minimum length of a pilot pulse
     */
    long MIN_PILOT_LENGTH = 1600;

    /**
     * The maximum length of a pilot pulse
     */
    long MAX_PILOT_LENGTH = 2560;

    Range<Long> PILOT_CANDIDATE_RANGE = Range.closed(MIN_PILOT_LENGTH, MAX_PILOT_LENGTH);

    /**
     * The minimum number of pilot pulses before a sync1 pulse can occur
     */
    int MIN_PILOT_COUNT = 32;

    /**
     * The maximum length the two sync pulses can be
     */
    int SYNC_TOTAL_MAX = 3594;

    /**
     * The standard length of a sync1 pulse
     */
    int SYNC1 = 667;

    /**
     * The standard length of a sync2 pulse
     */
    int SYNC2 = 735;

    /**
     * The longest valid length of two pulses to be considered as a bit in the data block
     */
    int DATA_TOTAL_MAX = 3950; // TODO: check this - loading two edges with a timing constant of 0xb0 for a bit implies 4450 as the total?

    /**
     * The maximum length of a tail pulse
     */
    int MAX_TAIL_PULSE = 1750;

    /**
     * The standard duration of a "one" bit pulse
     */
    short ONE = 1710;

    /**
     * The standard length of a "zero" bit pulse
     */
    short ZERO = 855;

    /**
     * The standard length of a tail pulse at the end of a data block
     */
    short TAIL = 945;

    /**
     * @return the current pulse being processed
     */
    long getCurrentPulse();

    /**
     * @return the next pulse to be processed
     */
    long getNextPulse();

    /**
     * @return true if there is another pulse to process after the current one
     */
    boolean hasNextPulse();

    /**
     * @return the length of the next pulse in the tape
     * @throws NoSuchElementException if there are no more pulses
     */
    Long peekNextPulse();

    /**
     * Get the resolution of each pulse in T-states (error is up to 2 samples)
     * @return the resolution of each pulse in T-states (error is up to 2 samples)
     */
    long getResolution();

    /**
     * All the required components of a data block have been processed, so complete
     * the block and add the completed block to the tape in progress. Will usually
     * add a separate block for likely sync pulses prior to the pulse block and a
     * trailing block with any unlikely data pulses following a tail.
     */
    void completeDataBlock();

    /**
     * All the required components of a pulse block have been processed, so complete
     * the block and add the completed block to the tape in progress.
     */
    void completeUnknownPulseBlock();

    /**
     * All the required components of a pulse block have been processed, so complete
     * the block and add the completed block to the tape in progress.
     */
    void completePilotPulseBlock();

    /**
     * Add the current pulse to the block in progress as a pilot pulse.
     */
    void addPilotPulse();

    /**
     * Add an unclassified pulse to the current block in progress
     * @param pulseLength the unclassified pulse
     */
    void addUnclassifiedPulse(Long pulseLength);

    /**
     * If a last block completed was being provisionally treated as a certain
     * type but subsequent data shows that is not correct, this method removes
     * the last classified block and adds its pulses to the beginning of the
     * pulses from the current block in progress. If no pulse blocks have been
     * added, has no effect.
     */
    void revertCurrentBlock();

    /**
     * @return the number of pilot pulses found in the block to date
     */
    int getNumPilotPulses();

    /**
     * Checks whether a pulse is a candidate to be a pilot pulse
     * @param pulse the pulse to validate
     * @return true if the pulse appears to be a pilot candidate
     */
    static boolean isaPilotCandidate(long pulse) {
        return PILOT_CANDIDATE_RANGE.contains(pulse);
    }

    /**
     * Checks whether a pulse is a candidate to be a data pulse
     * @param pulse the pulse to validate
     * @return true if the pulse appears to be a data candidate
     */
    static boolean isaDataCandidate(long pulse) {
        return pulse < MIN_PILOT_LENGTH;
    }
}
