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

import org.junit.Test;
import xyz.meunier.wav2pzx.PulseList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.primitives.Bytes.toArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXDataBlockTest {

    private int firstPulseLevel = 1;
    private Collection<Long> newPulses = Arrays.asList(200L, 200L);
    private final PulseList pulseList = new PulseList(newPulses, firstPulseLevel, 1);
    private long tailLength = 945;
    private int numBitsInLastByte = 8;
    private Collection<Byte> data = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x30);/* PULS *//* Length: 6 bytes *//* Initial pulse high *//* Repeat count 2 *//* Pulse length 200 */
    private final byte[] byteRepresentationOfPulses =
            new byte[]{(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                        (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 6 bytes */
                        (byte)0x00, (byte)0x00, /* Initial pulse high */
                        (byte)0x02, (byte)0x80, /* Repeat count 2 */
                        (byte)0xc8, (byte)0x00};

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithGoodChecksum() {
        Collection<Byte> goodData = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x30);
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, data);
        assertArrayEquals(getDataBytes((byte)0x18, goodData), instance.getPZXBlockDiskRepresentation());
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithBadChecksum() {
        // Checksum doesn't match, should result in PULS block rather than data block
        Collection<Byte> badData = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, badData);
        assertArrayEquals(getDataBytes((byte)0x18, badData), instance.getPZXBlockDiskRepresentation());
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithGoodChecksumButIncompleteLastByte() {
        Collection<Byte> badData = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);

        // Checksum matches but not all bits are present in the last byte,
		// should result in DATA block rather than data block
        PZXDataBlock instance = new PZXDataBlock(pulseList, 1, badData);
        assertArrayEquals(getDataBytes((byte)0x11, badData), instance.getPZXBlockDiskRepresentation());
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForATooShortBlock() {
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, data);
        assertThat("Block too short to be a header", instance.isHeader(), is(false));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForAHeader() {
        Collection<Byte> headerData = Arrays.asList(
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, headerData);
        assertThat("Block is a header", instance.isHeader(), is(true));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForANonHeader() {
        Collection<Byte> notHeaderData = Arrays.asList(
                (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, notHeaderData);
        assertThat("Header flag is not set", instance.isHeader(), is(false));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForAHeaderWithWrongLength() {
        Collection<Byte> notHeaderData = Arrays.asList(
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        PZXDataBlock instance =  new PZXDataBlock(pulseList, numBitsInLastByte, notHeaderData);
        assertThat("Header flag is set but length is wrong", instance.isHeader(), is(false));
    }

    /**
     * Test of checkChecksum method, of class PZXDataBlock.
     */
    @Test
    public void testCheckChecksumForAValidChecksumBlock() {
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, data);
        assertThat(instance.checkChecksum(), is(true));
    }

    /**
     * Test of checkChecksum method, of class PZXDataBlock.
     */
    @Test
    public void testCheckChecksumForAnInvalidChecksumBlock() {
        Collection<Byte> badChecksum = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);
        PZXDataBlock instance = new PZXDataBlock(pulseList, numBitsInLastByte, badChecksum);
        assertThat(instance.checkChecksum(), is(false));
    }

    private byte[] getDataBytes(byte bitCount, Collection<Byte> data) {
        List<Byte> retval = new ArrayList<>();
        retval.addAll(
                Arrays.asList(
                        (byte)0x44, (byte)0x41, (byte)0x54, (byte)0x41, /* DATA */
                        (byte)(0x10 + data.size()), (byte)0x00, (byte)0x00, (byte)0x00, /* size */
                          bitCount, (byte)0x00, (byte)0x00, (byte)0x80, /* bit count + initial pulse flag */
                        (byte)0xb1, (byte)0x03, /* tail length */
                        (byte)0x02, /* zero bit pulse length */
                        (byte)0x02, /* zero bit pulse length */
                        (byte)0x57, (byte)0x03, (byte)0x57, (byte)0x03, /* zero bit pulses */
                        (byte)0xae, (byte)0x06, (byte)0xae, (byte)0x06  /* one bit pulses */
                )
        );
        retval.addAll(data);
        return toArray(retval);
    }

}
