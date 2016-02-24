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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

/**
 * Representation of a sequence of pulses for a tape.
 * @author Fredrick Meunier
 */
public class PulseList {
    /**
     * The pulses
     */
    private final List<Double> pulseLengths;
    
    /**
     * The level of the first pulse of the sequence
     */
    private final int firstPulseLevel;

    /**
     * Get the list of pulses that comprise the tape
     * @return an immutable list of the pulses for the tape
     */
    public List<Double> getPulseLengths() {
        return pulseLengths;
    }

    /**
     * Get the level of the first pulse on the tape.
     * @return the level of the first pulse on the tape (0 or 1)
     */
    public int getFirstPulseLevel() {
        return firstPulseLevel;
    }
    
    /**
     * Constructor for a new PulseList
     * @param pulseLengths a non-empty list of pulses for a tape
     * @param firstPulseLevel the level of the first pulse in the list
     * @throws NullPointerException if the supplied list is null
     * @throws IllegalArgumentException if the supplied list is empty
     */
    public PulseList(Collection<Double> pulseLengths, int firstPulseLevel) {
        checkArgument(firstPulseLevel == 0 || firstPulseLevel == 1, "firstPulseLevel must be 0 or 1");
        this.pulseLengths = ImmutableList.copyOf(checkNotNull(pulseLengths));
        checkArgument(!pulseLengths.isEmpty(), "pulseLengths cannot be empty");
        this.firstPulseLevel = firstPulseLevel;
    }
}
