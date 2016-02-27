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
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import static xyz.meunier.wav2pzx.PZXEncodeUtils.addPZXBlockHeader;
import static xyz.meunier.wav2pzx.PZXEncodeUtils.putUnsignedLittleEndianShort;

/**
 * Represents a PZX pulse block (PULS).
 * <p>
 * Stores the pulses found on the tape and supports encoding them to the proper
 * disk format.
 * @author Fredrick Meunier
 */
public class PZXPulseBlock implements PZXBlock {
    
    // High bit of a 32 bit integer
    private static final long BIT_32_MASK =      0x80000000L;
    
    // Mask for bottom 31 bits
    private static final long LOW_31_BITS_MASK = 0x7fffffffL;
    
    // High bit of a 16 bit integer
    private static final int BIT_16_MASK =      0x8000;
    
    // Mask for bottom 15 bits
    private static final int LOW_15_BITS_MASK = 0x7fff;

    // First pulse level found in the block (0 or 1)
    private final int firstPulseLevel;
    
    // Immutable list of the pulses corresponding to the block found on the tape
    private final ImmutableList<Double> pulses;
    
    /**
     * Constructor for PZXPulseBlock.
     * @param firstPulseLevel the initial signal level for the block (0 or 1) 
     * @param newPulses the original tape pulses that have been decoded into this block
     * @throws NullPointerException if newPulses was null
     * @throws IllegalArgumentException if firstPulseLevel is not 0 or 1
     */
    public PZXPulseBlock(int firstPulseLevel, Collection<Double> newPulses) {
        checkArgument(firstPulseLevel == 0 || firstPulseLevel == 1, "firstPulseLevel should be 0 or 1");
        checkNotNull(newPulses, "newPulses must not be null");
        this.firstPulseLevel = firstPulseLevel;
        this.pulses = ImmutableList.copyOf(newPulses);
    }

    @Override
    public List<Double> getPulses() {
        return pulses;
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        // iterate through the pulse array doing a run length encoding of the number of repeated values
        PeekingIterator<Double> iterator = Iterators.peekingIterator(pulses.iterator());
        int count;
        // We will probably have a similar number of bytes output as source pulses * 2 16 bit values
        ArrayList<Byte> output = new ArrayList<>(pulses.size()*4);

        // The pulse level is low at start of the block by default. However initial
        // pulse of zero duration may be easily used to make it high.
        if( this.firstPulseLevel == 1 ) {
            addBytesFor(0, 1, output);
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
            addBytesFor(pulse, count, output);
        }
        
        return addPZXBlockHeader("PULS", output);
    }

    @Override
    public String getSummary() {
        return "first pulse level: " + firstPulseLevel + " pulse count: " + pulses.size();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.firstPulseLevel;
        hash = 23 * hash + Objects.hashCode(this.pulses);
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
        final PZXPulseBlock other = (PZXPulseBlock) obj;
        if (this.firstPulseLevel != other.firstPulseLevel) {
            return false;
        }
        if (!Objects.equals(this.pulses, other.pulses)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PZXPulseBlock{" + "firstPulseLevel=" + firstPulseLevel + ", pulses=" + pulses + '}';
    }

    /**
     * @return the level (0 or 1) for the first pulse in the block
     */
    @Override
    public int getFirstPulseLevel() {
        return firstPulseLevel;
    }

    /**
     * Utility function for adding pulses in the proper format for the PULS block
     * in the supplied list.
     * @param pulse the duration of the pulse to add
     * @param count the number of repeats of the pulse to add
     * @param output the destination for the bytes corresponding to the pulse sequence
     * @throws NullPointerException if output is null
     */
    public static void addBytesFor(long pulse, int count, List<Byte> output) {
        checkNotNull(output, "output list cannot be null");
        if( count < 1 ) return;

        /*
         * PULS - Pulse sequence
         * ---------------------
         * 
         * offset type   name      meaning
         * 0      u16    count     bits 0-14 optional repeat count (see bit 15), always greater than zero
         *                         bit 15 repeat count present: 0 not present 1 present
         * 2      u16    duration1 bits 0-14 low/high (see bit 15) pulse duration bits
         *                         bit 15 duration encoding: 0 duration1 1 ((duration1<<16)+duration2)
         * 4      u16    duration2 optional low bits of pulse duration (see bit 15 of duration1) 
         * 6      ...    ...       ditto repeated until the end of the block
         * 
         * The above can be summarized with the following pseudocode for decoding:
         * 
         *    count = 1 ;
         *    duration = fetch_u16() ;
         *    if ( duration > 0x8000 ) {
         *        count = duration & 0x7FFF ;
         *        duration = fetch_u16() ;
         *    }
         *    if ( duration >= 0x8000 ) {
         *        duration &= 0x7FFF ;
         *        duration <<= 16 ;
         *        duration |= fetch_u16() ;
         *    }
         * 
         * The pulse level is low at start of the block by default. However initial
         * pulse of zero duration may be easily used to make it high. Similarly, pulse
         * of zero duration may be used to achieve pulses lasting longer than
         * 0x7FFFFFFF T cycles. Note that if the repeat count is present in case of
         * zero pulse for some reason, any decoding implementation must consistently
         * behave as if there was one zero pulse if the repeat count is odd and as if
         * there was no such pulse at all if it is even.
         */
        
        if( pulse > BIT_32_MASK  ) {
            // Encode as "very long pulse" - a stream of one count pulses 
            // interspersed with 1 count, 0 duration pulses to maintain the level
            for(int i = 0; i < count; i++) {
                // write pulse / 0x7fffffff long pulses with 0 duration one cycle
                // block in between to preserve pulse level
                for(long j = 0; j < pulse / LOW_31_BITS_MASK; j++) {
                    writeMultiCyclePulse(LOW_31_BITS_MASK, (short)1, output);
                    writeOneCycleShortPulse((short)0, output);
                }
                // Write remainder of pulse if required
                if(pulse % LOW_31_BITS_MASK > 0) {
                    // write pulse mod 0x7fffffff long pulse
                    addBytesFor(pulse % LOW_31_BITS_MASK, 1, output);
                } else {
                    // Remove the unneeded last 0 cycle bit flipper
                    output.remove(output.size()-1);
                    output.remove(output.size()-1);
                }
            }
        } else {
            if( count == 1 ) {
                // can pack in 16 bits if duration is also <= 0x7fff cycles
                if( pulse <= LOW_15_BITS_MASK ) {
                    writeOneCycleShortPulse((short)pulse, output);
                } else {
                    writeMultiCyclePulse(pulse, (short)count, output);
                }
            } else {
                if( count <= LOW_31_BITS_MASK ) {
                    writeMultiCyclePulse(pulse, (short)count, output);
                } else {
                    for(int j = 0; j < count / LOW_31_BITS_MASK; j++) {
                        writeMultiCyclePulse(pulse, (short)LOW_31_BITS_MASK, output);
                    }
                    if(count % LOW_31_BITS_MASK > 0) {
                        // write remaining count for this pulse
                        writeMultiCyclePulse(pulse, (short)(count % LOW_31_BITS_MASK), output);
                    }
                }
            }
        }
    }

    // Write the packed one cycle 16 bit pulse to the file
    private static void writeOneCycleShortPulse(short pulse, List<Byte> output) {
        /*
         * offset type   name      meaning
         * 0      u16    count     bits 0-14 optional repeat count (see bit 15), always greater than zero
         *                         bit 15 repeat count present: 0 not present 1 present
         */
        // In this case the byte encoding of the pulses is a little endian
        // unsigned 16 bit integer set to the number of cycles (bit 15 is 0 so
        // unsigned representation matches signed)
        putUnsignedLittleEndianShort(pulse, output);
    }

    // This writes a pulse with a repeat count of more than one and a duration
    // of less than 0x7fffffff cycles
    private static void writeMultiCyclePulse(long duration, short count, List<Byte> output) {
        /*
         * offset type   name      meaning
         * 0      u16    count     bits 0-14 optional repeat count (see bit 15), always greater than zero
         *                         bit 15 repeat count present: 1 present
         * 2      u16    duration1 bits 0-14 low/high (see bit 15) pulse duration bits
         *                         bit 15 duration encoding: 0 duration1 1 ((duration1<<16)+duration2)
         * 4      u16    duration2 optional low bits of pulse duration (see bit 15 of duration1) 
         */
        // Count is required to be 0x7fff cycles or less, set high bit to represent repeat count is present
        int outputValue = BIT_16_MASK | count; 
        putUnsignedLittleEndianShort((short)outputValue, output);
        
        if( duration > LOW_15_BITS_MASK ) {
            // duration1
            outputValue = (short)((duration >> 16) & LOW_15_BITS_MASK | BIT_16_MASK);
            putUnsignedLittleEndianShort((short)outputValue, output);
            
            // duration2
            outputValue = (short)(duration & 0xffff);
            putUnsignedLittleEndianShort((short)outputValue, output);
            return;
        }
        
        // duration1
        outputValue = (short)(duration & LOW_15_BITS_MASK);
        putUnsignedLittleEndianShort((short)outputValue, output);
    }

}
