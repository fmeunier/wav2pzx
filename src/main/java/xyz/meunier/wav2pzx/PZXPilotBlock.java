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
import static xyz.meunier.wav2pzx.PZXEncodeUtils.addPZXBlockHeader;
import static xyz.meunier.wav2pzx.LoaderContext.SYNC1;
import static xyz.meunier.wav2pzx.LoaderContext.SYNC2;
import static xyz.meunier.wav2pzx.PZXPulseBlock.addBytesFor;

/**
 * This is a specialisation of the PULS PZXPulseBlock that is used when we have
 * identified the standard ZX ROM loading pulses.
 * <p>
 * It uses the known timing pulse durations derived from the ROM code to represent
 * its elements.
 * <p>
 * TODO: add support for optionally using timings derived from provided file
 * @author Fredrick Meunier
 */
public class PZXPilotBlock extends PZXPulseBlock {
    
    // The length of the SYNC1 pulse found on tape
    private final double sync1Length;
    
    // The length of the SYNC2 pulse found on tape
    private final double sync2Length;

    /**
     * Constructor for the PZXPilotBlock.
     * @param firstPulseLevel the initial signal level for the block (0 or 1) 
     * @param newPulses the original tape pulses that have been decoded into this block
     * @param sync1Length the length of the SYNC1 pulse found on tape
     * @param sync2Length the length of the SYNC2 pulse found on tape 
     * @throws NullPointerException if newPulses was null
     * @throws IllegalArgumentException if firstPulseLevel is not 0 or 1
     */
    public PZXPilotBlock(int firstPulseLevel, Collection<Double> newPulses, 
                        double sync1Length, double sync2Length) {
        super(firstPulseLevel, newPulses);
        this.sync1Length = sync1Length;
        this.sync2Length = sync2Length;
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        // Same block as PZX Pulse Block but optimised representation based on
        // detected structure
        ArrayList<Byte> output = new ArrayList<>();
        
        // The pulse level is low at start of the block by default. However initial
        // pulse of zero duration may be easily used to make it high.
        if( getFirstPulseLevel() == 1 ) {
            addBytesFor(0, 1, output);
        }
        
        addBytesFor(LoaderContext.PILOT_LENGTH, getPulses().size(), output);
        addBytesFor(LoaderContext.SYNC1, 1, output);
        addBytesFor(LoaderContext.SYNC2, 1, output);
        
        return addPZXBlockHeader("PULS", output);
    }
    
    @Override
    public String getSummary() {
        StringBuilder retval = new StringBuilder();

        retval.append("Sync1 pulse:").append(sync1Length).append(" tstates, ")
                .append((double)sync1Length/SYNC1*100).append("% of expected\n");
        retval.append("Sync2 pulse:").append(sync2Length).append(" tstates, ")
                .append((double)sync2Length/SYNC2*100).append("% of expected\n");

        retval.append(super.getSummary());

        return retval.toString();
    }

    @Override
    public String toString() {
        return "PZXPilotBlock{" + super.getSummary() + ", sync1Length=" + sync1Length + ", sync2Length=" + sync2Length + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.sync1Length) ^ (Double.doubleToLongBits(this.sync1Length) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.sync2Length) ^ (Double.doubleToLongBits(this.sync2Length) >>> 32));
        hash = 67 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PZXPilotBlock other = (PZXPilotBlock) obj;
        if (Double.doubleToLongBits(this.sync1Length) != Double.doubleToLongBits(other.sync1Length)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sync2Length) != Double.doubleToLongBits(other.sync2Length)) {
            return false;
        }
        if (this.getFirstPulseLevel() != other.getFirstPulseLevel()) {
            return false;
        }
        if (!Objects.equals(this.getPulses(), other.getPulses())) {
            return false;
        }
        return true;
    }
    
}
