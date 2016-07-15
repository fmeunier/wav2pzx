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

import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds a processed full pulse and the range of raw full pulse lengths that evaluate to this new full pulse
 */
public final class BitData {
    private final Range<Long> qualificationRange;
    private final Long fullPulse;

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
    }

    Range<Long> getQualificationRange() {
        return qualificationRange;
    }

    Long getFullPulse() {
        return fullPulse;
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
                '}';
    }
}
