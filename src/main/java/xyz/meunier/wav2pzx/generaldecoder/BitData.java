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
import com.google.common.collect.Range;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

/**
 * Holds a processed full pulse and the range of raw full pulse lengths that evaluate to this new full pulse. For
 * pulse blocks this would likely be a single pulse per, for data blocks we expect two pulses per bit.
 */
public final class BitData {
    private static final BitData NULL_BIT_DATA = new BitData(Range.lessThan(0L), emptyList());
    private final Range<Long> qualificationRange;
    private final Long fullPulse;
    private final ImmutableList<Long> pulses;

    /**
     * @return a null bit data item that will match no pulses and has no pulses
     */
    static BitData NullBitData() {
        return NULL_BIT_DATA;
    }

    /**
     * Makes a new BitData object.
     * @param qualificationRange the ranges of full pulses that qualify as equal to this pulse
     * @param fullPulse the processed full pulse value to replace qualifying raw pulses for
     */
    public BitData(Range<Long> qualificationRange, Long fullPulse) {
        checkNotNull(qualificationRange, "qualificationRange was null");
        checkNotNull(fullPulse, "fullPulse was null");
        this.qualificationRange = qualificationRange;
        this.fullPulse = fullPulse;
        long element = fullPulse / 2;
        this.pulses = ImmutableList.of(element, element);
    }

    /**
     * Makes a new BitData object.
     * @param qualificationRange the ranges of full pulses that qualify as equal to this pulse
     * @param pulses a list of pulses for this bit
     */
    public BitData(Range<Long> qualificationRange, List<Long> pulses) {
        checkNotNull(qualificationRange, "qualificationRange was null");
        checkNotNull(pulses, "pulses was null");
        this.qualificationRange = qualificationRange;
        this.fullPulse = pulses.stream().mapToLong(Long::longValue).sum();
        this.pulses = ImmutableList.copyOf(pulses);
    }

    Range<Long> getQualificationRange() {
        return qualificationRange;
    }

    Long getFullPulse() {
        return fullPulse;
    }

    public ImmutableList<Long> getPulses() {
        return pulses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitData bitData = (BitData) o;

        if (!qualificationRange.equals(bitData.qualificationRange)) return false;
        return fullPulse.equals(bitData.fullPulse);

    }

    @Override
    public int hashCode() {
        int result = qualificationRange.hashCode();
        result = 31 * result + fullPulse.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BitData{" +
                "qualificationRange=" + qualificationRange +
                ", fullPulse=" + fullPulse +
                ", pulses=" + pulses +
                '}';
    }

    static int compare(BitData a, BitData b) {
        return Long.compare(a.getFullPulse(), b.getFullPulse());
    }
}
