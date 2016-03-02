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

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.ArrayList;
import java.util.List;
import static xyz.meunier.wav2pzx.PZXEncodeUtils.addPZXBlockHeader;

/**
 * Represents a PZX pulse block (PULS).
 * <p>
 * Stores the pulses found on the tape and supports encoding them to the proper
 * disk format.
 * @author Fredrick Meunier
 */
public class PZXPulseBlock implements PZXBlock {
    
    // Details of original pulses corresponding to block
    private final PulseList pulseList;
    
    /**
     * Constructor for PZXPulseBlock.
     * @param newPulses the original tape pulses that have been decoded into this block
     * @throws NullPointerException if newPulses was null
     */
    public PZXPulseBlock(PulseList newPulses) {
        checkNotNull(newPulses, "newPulses must not be null");
        this.pulseList = newPulses;
    }

    @Override
    public List<Double> getPulses() {
        return pulseList.getPulseLengths();
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        // iterate through the pulse array doing a run length encoding of the number of repeated values
        PeekingIterator<Double> iterator = Iterators.peekingIterator(getPulses().iterator());
        int count;
        // We will probably have a similar number of bytes output as source pulses * 2 16 bit values
        ArrayList<Byte> output = new ArrayList<>(getPulses().size()*4);

        // The pulse level is low at start of the block by default. However initial
        // pulse of zero duration may be easily used to make it high.
        if( pulseList.getFirstPulseLevel() == 1 ) {
            PZXEncodeUtils.addBytesFor(0, 1, output);
        }

        // RLE the pulses found in the block for encoding
        while(iterator.hasNext()) {
            long pulse = Math.round(iterator.next());
            count = 1;
            while(iterator.hasNext() && Math.round(iterator.peek()) == pulse) { 
                iterator.next();
                count += 1;
            }
            
            // Write the desired output bytes to the output list
            PZXEncodeUtils.addBytesFor(pulse, count, output);
        }
        
        return addPZXBlockHeader("PULS", output);
    }

    @Override
    public String getSummary() {
        return "first pulse level: " + pulseList.getFirstPulseLevel() + " pulse count: " + pulseList.getPulseLengths().size();
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pulseList == null) ? 0 : pulseList.hashCode());
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
		PZXPulseBlock other = (PZXPulseBlock) obj;
		if (pulseList == null) {
			if (other.pulseList != null)
				return false;
		} else if (!pulseList.equals(other.pulseList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PZXPulseBlock [pulseList=" + pulseList + "]";
	}

	/**
     * @return the level (0 or 1) for the first pulse in the block
     */
    @Override
    public int getFirstPulseLevel() {
        return pulseList.getFirstPulseLevel();
    }

}
