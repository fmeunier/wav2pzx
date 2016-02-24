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

/**
 * This class has utility functions to assist in writing data in the conventions 
 * of PZX files
 * @author Fredrick Meunier
 */
public class PZXEncodeUtils {
    
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
}
