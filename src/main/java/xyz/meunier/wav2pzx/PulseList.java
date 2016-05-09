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

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of an immutable sequence of pulses for a tape.
 * @author Fredrick Meunier
 */
public final class PulseList {
    /**
     * The pulses
     */
    private final ImmutableList<Long> pulseLengths;
    
    /**
     * The level of the first pulse of the sequence
     */
    private final int firstPulseLevel;

    /**
     * The resolution of each pulse in T-states (error is up to 2 samples)
     */
    private final long resolution;
    
    /**
     * Get the list of pulses that comprise the tape
     * @return an immutable list of the pulses for the tape
     */
    public ImmutableList<Long> getPulseLengths() {
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
     * Get the resolution of each pulse in T-states (error is up to 2 samples)
     * @return the resolution of each pulse in T-states (error is up to 2 samples)
     */
    public long getResolution() {
		return resolution;
	}

	/**
     * Constructor for a new PulseList
     * @param pulseLengths a non-empty Iterable of pulses for a tape
     * @param firstPulseLevel the level of the first pulse in the list
     * @param resolution the resolution of each pulse in T-states (error is up to 2 samples)
     * @throws NullPointerException if the supplied list is null
     * @throws IllegalArgumentException if the supplied list is empty
     * @throws IllegalArgumentException if firstPulseLevel is not 0 or 1
     */
    public PulseList(Iterable<Long> pulseLengths, int firstPulseLevel, long resolution) {
        checkNotNull(pulseLengths, "pulseLengths must not be null");
        checkArgument(firstPulseLevel == 0 || firstPulseLevel == 1, "firstPulseLevel must be 0 or 1");
        this.pulseLengths = ImmutableList.copyOf(checkNotNull(pulseLengths));
        checkArgument(!this.pulseLengths.isEmpty(), "pulseLengths cannot be empty");
        this.firstPulseLevel = firstPulseLevel;
        this.resolution = resolution;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + firstPulseLevel;
		result = prime * result + ((pulseLengths == null) ? 0 : pulseLengths.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PulseList other = (PulseList) obj;
		if (firstPulseLevel != other.firstPulseLevel)
			return false;
		if (pulseLengths == null) {
			if (other.pulseLengths != null)
				return false;
		} else if (!pulseLengths.equals(other.pulseLengths))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PulseList [pulseLengths.size()=" + pulseLengths.size() + ", firstPulseLevel=" + firstPulseLevel
				+ ", resolution=" + resolution + "]";
	}

	/**
	 * Returns a String representation of a PulseList in the non-annotated text format that is used by the pzx2txt
	 * program in the pzxtools
	 * @return the String representation
     */
	public String toPulseListText() {
		StringBuilder builder = new StringBuilder();
		builder.append("PULSES\n");
		pulseLengths.stream().forEach(p -> builder.append("PULSE ").append(p).append("\n"));
		return builder.toString();
	}
}
