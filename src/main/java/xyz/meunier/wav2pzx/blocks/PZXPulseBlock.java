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
package xyz.meunier.wav2pzx.blocks;

import static com.google.common.base.Preconditions.checkNotNull;
import static xyz.meunier.wav2pzx.blocks.PZXEncodeUtils.addPZXBlockHeader;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import xyz.meunier.wav2pzx.PulseList;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a PZX pulse block (PULS).
 * <p>
 * Stores the pulses found on the tape and supports encoding them to the proper
 * disk format.
 * @author Fredrick Meunier
 */
public final class PZXPulseBlock implements PZXBlock {
    
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
        // TODO: Scan pulses placing them into buckets of effectively equal lengths, when
        // we have a reasonable amount of pulses in a bucket, replace pulses in the source list 
        // with the average value from the bucket. This would be to try and optimise the storage
        // of the source pulses - should be optional.
    }

    @Override
    public List<Long> getPulses() {
        return pulseList.getPulseLengths();
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        return getPZXBlockDiskRepresentation(pulseList);
    }

	/**
     * Return the on-disk PZX format data for the supplied PulseList
	 * @param pulseList the PulseList to encode into the disk representation
     * @return the byte[] with the PZX disk format data
   	 */
	public static byte[] getPZXBlockDiskRepresentation(PulseList pulseList) {
		// iterate through the pulse array doing a run length encoding of the number of repeated values
        PeekingIterator<Long> iterator = Iterators.peekingIterator(pulseList.getPulseLengths().iterator());
        int count;
        // We will probably have a similar number of bytes output as source pulses * 2 16 bit values
        ArrayList<Byte> output = new ArrayList<>(pulseList.getPulseLengths().size()*4);

        // The pulse level is low at start of the block by default. However initial
        // pulse of zero duration may be easily used to make it high.
        if( pulseList.getFirstPulseLevel() == 1 ) {
            PZXEncodeUtils.addBytesFor(0, 1, output);
        }

        // RLE the pulses found in the block for encoding
        while(iterator.hasNext()) {
            long pulse = iterator.next();
            count = 1;
            while(iterator.hasNext() && iterator.peek() == pulse) { 
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
