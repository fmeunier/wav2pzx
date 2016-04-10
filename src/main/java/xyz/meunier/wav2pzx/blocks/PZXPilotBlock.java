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

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

import xyz.meunier.wav2pzx.LoaderContext;
import xyz.meunier.wav2pzx.PulseList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static xyz.meunier.wav2pzx.LoaderContext.SYNC1;
import static xyz.meunier.wav2pzx.LoaderContext.SYNC2;
import static xyz.meunier.wav2pzx.blocks.PZXEncodeUtils.addPZXBlockHeader;

/**
 * This is a specialisation of the PULS PZX data block that is used when we have
 * identified the standard ZX ROM loading pulses.
 * <p>
 * It uses the known timing pulse durations derived from the ROM code to represent
 * its elements.
 * <p>
 * TODO: add support for optionally using timings derived from provided file
 * @author Fredrick Meunier
 */
public final class PZXPilotBlock implements PZXBlock {
    
    // Details of original pulses corresponding to block
	private final PulseList pulses;
	
    // The length of the SYNC1 pulse found on tape
    private final long sync1Length;
    
    // The length of the SYNC2 pulse found on tape
    private final long sync2Length;

    /**
     * Constructor for the PZXPilotBlock.
     * @param newPulses the original tape pulses that have been decoded into this block
     * @throws NullPointerException if newPulses was null
     * @throws IllegalArgumentException if newPulses has fewer than 3 pulses (the initial pilot plus the two sync pulses)
     */
    public PZXPilotBlock(PulseList newPulses) {
        checkNotNull(newPulses, "newPulses must not be null");
        checkArgument(newPulses.getPulseLengths().size() > 2, "newPulses needs at least 3 pulses");
        this.pulses = newPulses;
        List<Long> pulses = getPulses();
		this.sync1Length = pulses.get(pulses.size() - 2);
        this.sync2Length = pulses.get(pulses.size() - 1);
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        // Same block as PZX Pulse Block but optimised representation based on
        // detected structure
        ArrayList<Byte> output = new ArrayList<>();
        
        // The pulse level is low at start of the block by default. However initial
        // pulse of zero duration may be easily used to make it high.
        if( getFirstPulseLevel() == 1 ) {
            PZXEncodeUtils.addBytesFor(0, 1, output);
        }
        
        PZXEncodeUtils.addBytesFor(LoaderContext.PILOT_LENGTH, getPulses().size() - 2, output);
        PZXEncodeUtils.addBytesFor(LoaderContext.SYNC1, 1, output);
        PZXEncodeUtils.addBytesFor(LoaderContext.SYNC2, 1, output);
        
        return addPZXBlockHeader("PULS", output);
    }
    
    @Override
    public String getSummary() {
        StringBuilder retval = new StringBuilder("PZXPilotBlock:\n");

        LongSummaryStatistics stats = 
        		pulses.getPulseLengths()
        			.subList(0, pulses.getPulseLengths().size() - 2)
        			.stream().mapToLong(x -> x).summaryStatistics();
        
        retval.append("Average pilot pulse:").append(Math.round(stats.getAverage())).append(" tstates, ")
        		.append(String.format("%.2f", stats.getAverage()/LoaderContext.PILOT_LENGTH*100.0)).append("% of expected\n");
        
        retval.append("Sync1 pulse:").append(sync1Length).append(" tstates, ")
                .append(String.format("%.2f", (double)sync1Length/SYNC1*100.0)).append("% of expected\n");
        retval.append("Sync2 pulse:").append(sync2Length).append(" tstates, ")
                .append(String.format("%.2f", (double)sync2Length/SYNC2*100.0)).append("% of expected\n");

        retval.append(pulses.toString());

        return retval.toString();
    }

    @Override
    public String toString() {
        return "PZXPilotBlock{" + pulses.toString() + ", sync1Length=" + sync1Length + ", sync2Length=" + sync2Length + '}';
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pulses == null) ? 0 : pulses.hashCode());
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
		PZXPilotBlock other = (PZXPilotBlock) obj;
		if (pulses == null) {
			if (other.pulses != null)
				return false;
		} else if (!pulses.equals(other.pulses))
			return false;
		return true;
	}

	@Override
	public List<Long> getPulses() {
		return pulses.getPulseLengths();
	}

	@Override
	public int getFirstPulseLevel() {
		return pulses.getFirstPulseLevel();
	}
    
}
