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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * The aim of this class is to take a series of pulses and build a PulseList
 *
 * @author Fredrick Meunier
 */
public final class PulseListBuilder {

    private final Collection<Long> pulseLengths = new ArrayList<>();
    private int firstPulseLevel = 0;
    private long resolution = 1;

    /**
     * Set the first pulse signal level
     * @param firstPulseLevel the first pulse level of this block, needs to be 0 or 1
     * @return this builder instance
     * @throws IllegalArgumentException if firstPulseLevel is not 0 or 1
     */
    public PulseListBuilder withFirstPulseLevel(int firstPulseLevel) {
        checkArgument(firstPulseLevel == 0 || firstPulseLevel == 1, "firstPulseLevel must be 0 or 1");
        this.firstPulseLevel = firstPulseLevel;
        return this;
    }

    /**
     * Set the sample resolution of these pulses in units of 1/3,500,000ths of a second
     * @param resolution the sample resolution of these pulses in 3,500,000 Hz tstates
     * @return this builder instance
     * @throws IllegalArgumentException if resolution is not greater than 0
     */
    public PulseListBuilder withResolution(long resolution) {
        checkArgument(resolution > 0, "resolution must be greater than 0");
        this.resolution = resolution;
        return this;
    }

    /**
     * Add a pulse with the supplied duration in tstates.
     * @param pulse the length of this pulse in 3,500,000 Hz tstates
     * @return this builder instance
     * @throws IllegalArgumentException if pulse is null or not greater than or equal to 0
     */
    public PulseListBuilder withNextPulse(Long pulse) {
        checkArgument(pulse != null, "pulse cannot be null");
        checkArgument(pulse >= 0, "pulse must be greater than or equal to 0 tstates");
        pulseLengths.add(pulse);
        return this;
    }

    /**
     * Add a collection of non-null pulses with the specified durations in tstates.
     * @param pulses a collection of pulses in 3,500,000 Hz tstates size
     * @return this builder instance
     * @throws IllegalArgumentException if pulse is null or not greater than or equal to 0
     */
    public PulseListBuilder withNextPulses(Collection<Long> pulses) {
        pulses.stream().filter(Objects::nonNull).forEach(this::withNextPulse);
        return this;
    }

    /**
     * Construct the new PulseList and mark the tape as being complete
     * @return the PulseList
     * @throws IllegalStateException if we haven't yet processed any samples from the tape
     */
    public PulseList build() {
        // State error, haven't received first pulse so we don't know the first pulse level
        checkState(!pulseLengths.isEmpty(), "First pulse not yet received");
        return new PulseList(pulseLengths, firstPulseLevel, resolution);
    }

    /**
     * @return the first pulse signal level of the pulse list
     */
    public int getFirstPulseLevel() {
        return firstPulseLevel;
    }

    /**
     * @return the sample resolution of the pulse list
     */
    public long getResolution() {
        return resolution;
    }

    /**
     * @return true if the builder is empty
     */
    public boolean isEmpty() {
        return pulseLengths.isEmpty();
    }
}
