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
import com.google.common.primitives.Bytes;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * This class has utility functions to assist in writing data in the conventions 
 * of PZX files
 * @author Fredrick Meunier
 */
public class PZXEncodeUtils {

	// High bit of a 32 bit integer
	static private final long BIT_32_MASK =      0x80000000L;
	// Mask for bottom 31 bits
	static private final long LOW_31_BITS_MASK = 0x7fffffffL;
	// High bit of a 16 bit integer
	static private final int BIT_16_MASK =      0x8000;
	// Mask for bottom 15 bits
	static private final int LOW_15_BITS_MASK = 0x7fff;
    
    /**
     * Returns the disk representation of an encoded PZX block containing the 
     * data from the supplied byte collection.
     * @param headerTag the tag for the resulting PZX block
     * @param output the data payload of the resulting block
     * @return a byte array with the data for the supplied array
     * @throws NullPointerException if the supplied header or collection are null
     */
    public static byte[] addPZXBlockHeader(String headerTag, Collection<Byte> output) {
        checkNotNull(output);
        return addPZXBlockHeader(headerTag, Bytes.toArray(output));
    }

    /**
     * Returns the disk representation of an encoded PZX block containing the 
     * data from the supplied byte array.
     * @param headerTag the tag for the resulting PZX block
     * @param output the data payload of the resulting block
     * @return a byte array with the data for the supplied array
     * @throws NullPointerException if the supplied header or collection are null
     */
    public static byte[] addPZXBlockHeader(String headerTag, byte[] output) {
        checkNotNull(headerTag);
        checkNotNull(output);
        
        /* The PZX file format consists of a sequence of blocks. Each block has the
           following uniform structure:

           offset type     name   meaning
           0      u32      tag    unique identifier for the block type.
           4      u32      size   size of the block in bytes, excluding the tag and size fields themselves.
           8      u8[size] data   arbitrary amount of block data.

           The block tags are four ASCII letter identifiers indicating how to interpret the
           block data. The first letter is stored first in the file (i.e., at
           offset 0 of the block), the last letter is stored last (i.e., at offset 3).
           This means the tag may be internally conveniently represented either as 32bit
           multicharacter constants stored in big endian order or 32bit reversed
           multicharacter constant stored in little endian order, whichever way an
           implementation prefers.
        */

        int size = output.length;
        byte[] buffer = ByteBuffer.allocate(8 + size)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(headerTag.getBytes(Charset.forName("US-ASCII"))) // TODO support Sinclair character set
                .putInt(size).put(output).array();
        
        return buffer;
    }

    /**
     * Encode the provided integer to the supplied byte collection in unsigned 
     * little endian format.
     * @param outputVal the integer to encode to the supplied collection
     * @param output the destination for the encoded integer
     * @throws NullPointerException if the supplied collection is null
     */
    public static void putUnsignedLittleEndianInt(int outputVal, Collection<Byte> output) {
        checkNotNull(output);
        
        byte[] buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(outputVal).array();
        output.addAll(Bytes.asList(buffer));
    }
    
    /**
     * Writes an unsigned 16 bit value in little endian format to the supplied 
     * Byte Collection
     * @param outputVal the short to encode to the supplied collection
     * @param output the destination for the encoded short
     * @throws NullPointerException if the supplied collection is null
     */
    public static void putUnsignedLittleEndianShort(short outputVal, Collection<Byte> output) {
        checkNotNull(output);
        
        byte[] buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short)outputVal).array();
        output.addAll(Bytes.asList(buffer));
    }

    /**
     * Writes an 8 bit value to the supplied Byte Collection
     * @param outputVal the short to encode to the supplied collection
     * @param output the destination for the encoded short
     * @throws NullPointerException if the supplied collection is null
     */
    public static void putUnsignedByte(byte outputVal, Collection<Byte> output) {
        checkNotNull(output);
        
        output.add(outputVal);
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
	    
	    if( pulse > PZXEncodeUtils.BIT_32_MASK  ) {
	        // Encode as "very long pulse" - a stream of one count pulses 
	        // interspersed with 1 count, 0 duration pulses to maintain the level
	        for(int i = 0; i < count; i++) {
	            // write pulse / 0x7fffffff long pulses with 0 duration one cycle
	            // block in between to preserve pulse level
	            for(long j = 0; j < pulse / PZXEncodeUtils.LOW_31_BITS_MASK; j++) {
	                PZXEncodeUtils.writeMultiCyclePulse(PZXEncodeUtils.LOW_31_BITS_MASK, (short)1, output);
	                PZXEncodeUtils.writeOneCycleShortPulse((short)0, output);
	            }
	            // Write remainder of pulse if required
	            if(pulse % PZXEncodeUtils.LOW_31_BITS_MASK > 0) {
	                // write pulse mod 0x7fffffff long pulse
	                addBytesFor(pulse % PZXEncodeUtils.LOW_31_BITS_MASK, 1, output);
	            } else {
	                // Remove the unneeded last 0 cycle bit flipper
	                output.remove(output.size()-1);
	                output.remove(output.size()-1);
	            }
	        }
	    } else {
	        if( count == 1 ) {
	            // can pack in 16 bits if duration is also <= 0x7fff cycles
	            if( pulse <= PZXEncodeUtils.LOW_15_BITS_MASK ) {
	                PZXEncodeUtils.writeOneCycleShortPulse((short)pulse, output);
	            } else {
	                PZXEncodeUtils.writeMultiCyclePulse(pulse, (short)count, output);
	            }
	        } else {
	            if( count <= PZXEncodeUtils.LOW_31_BITS_MASK ) {
	                PZXEncodeUtils.writeMultiCyclePulse(pulse, (short)count, output);
	            } else {
	                for(int j = 0; j < count / PZXEncodeUtils.LOW_31_BITS_MASK; j++) {
	                    PZXEncodeUtils.writeMultiCyclePulse(pulse, (short)PZXEncodeUtils.LOW_31_BITS_MASK, output);
	                }
	                if(count % PZXEncodeUtils.LOW_31_BITS_MASK > 0) {
	                    // write remaining count for this pulse
	                    PZXEncodeUtils.writeMultiCyclePulse(pulse, (short)(count % PZXEncodeUtils.LOW_31_BITS_MASK), output);
	                }
	            }
	        }
	    }
	}

	// Write the packed one cycle 16 bit pulse to the file
	static private void writeOneCycleShortPulse(short pulse, List<Byte> output) {
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
	static private void writeMultiCyclePulse(long duration, short count, List<Byte> output) {
	    /*
	     * offset type   name      meaning
	     * 0      u16    count     bits 0-14 optional repeat count (see bit 15), always greater than zero
	     *                         bit 15 repeat count present: 1 present
	     * 2      u16    duration1 bits 0-14 low/high (see bit 15) pulse duration bits
	     *                         bit 15 duration encoding: 0 duration1 1 ((duration1<<16)+duration2)
	     * 4      u16    duration2 optional low bits of pulse duration (see bit 15 of duration1) 
	     */
	    // Count is required to be 0x7fff cycles or less, set high bit to represent repeat count is present
	    int outputValue = PZXEncodeUtils.BIT_16_MASK | count; 
	    putUnsignedLittleEndianShort((short)outputValue, output);
	    
	    if( duration > PZXEncodeUtils.LOW_15_BITS_MASK ) {
	        // duration1
	        outputValue = (short)((duration >> 16) & PZXEncodeUtils.LOW_15_BITS_MASK | PZXEncodeUtils.BIT_16_MASK);
	        putUnsignedLittleEndianShort((short)outputValue, output);
	        
	        // duration2
	        outputValue = (short)(duration & 0xffff);
	        putUnsignedLittleEndianShort((short)outputValue, output);
	        return;
	    }
	    
	    // duration1
	    outputValue = (short)(duration & PZXEncodeUtils.LOW_15_BITS_MASK);
	    putUnsignedLittleEndianShort((short)outputValue, output);
	}

}
