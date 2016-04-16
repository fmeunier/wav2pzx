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

import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.meunier.wav2pzx.PulseList;
import xyz.meunier.wav2pzx.blocks.PZXDataBlock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXDataBlockTest {
    
    int firstPulseLevel = 1;
    Collection<Long> newPulses = Arrays.asList(200L, 200L);
    long tailLength = 945;
    int numBitsInLastByte = 8;
    Collection<Byte> data = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x30);

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithGoodChecksum() {
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel, 1),
                                                 tailLength, numBitsInLastByte, data);
        byte[] expResult = {(byte)0x44, (byte)0x41, (byte)0x54, (byte)0x41, /* DATA */
                            (byte)0x13, (byte)0x00, (byte)0x00, (byte)0x00, /* size */
                            (byte)0x18, (byte)0x00, (byte)0x00, (byte)0x80, /* bit count + initial pulse flag */
                            (byte)0xb1, (byte)0x03, /* tail length */
                            (byte)0x02, /* zero bit pulse length */
                            (byte)0x02, /* zero bit pulse length */
                            (byte)0x57, (byte)0x03, (byte)0x57, (byte)0x03, /* zero bit pulses */
                            (byte)0xae, (byte)0x06, (byte)0xae, (byte)0x06, /* one bit pulses */
                            (byte)0x10, (byte)0x20, (byte)0x30 /* data */};
        byte[] result = instance.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithBadChecksum() {
        // Checksum doesn't match, should result in PULS block rather than data block
        Collection<Byte> badData = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel, 1),
                                                 tailLength, numBitsInLastByte, badData);
        byte[] expResult = {(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                            (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 6 bytes */
                            (byte)0x00, (byte)0x00, /* Initial pulse high */
                            (byte)0x02, (byte)0x80, /* Repeat count 2 */
                            (byte)0xc8, (byte)0x00};/* Pulse length 200 */
        byte[] result = instance.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentationForPZXDataBlockWithGoodChecksumButIncompleteLastByte() {
        Collection<Byte> badData = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);

        // Checksum matches but not all bits are present in the last byte,
		// should result in PULS block rather than data block
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel, 1),
		                tailLength, 1, badData);
        byte[] expResult = {(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                            (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 6 bytes */
                            (byte)0x00, (byte)0x00, /* Initial pulse high */
                            (byte)0x02, (byte)0x80, /* Repeat count 2 */
                            (byte)0xc8, (byte)0x00};/* Pulse length 200 */
        byte[] result = instance.getPZXBlockDiskRepresentation();
		assertArrayEquals(expResult, result);
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForATooShortBlock() {
        PulseList newPulses = new PulseList(this.newPulses, firstPulseLevel, 1);
        PZXDataBlock instance = new PZXDataBlock(newPulses, tailLength, numBitsInLastByte, data);
        assertThat("Block too short to be a header", instance.isHeader(), is(false));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForAHeader() {
        PulseList newPulses = new PulseList(this.newPulses, firstPulseLevel, 1);
        Collection<Byte> headerData = Arrays.asList(
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        PZXDataBlock instance = new PZXDataBlock(newPulses, tailLength, numBitsInLastByte, headerData);
        assertThat("Block is a header", instance.isHeader(), is(true));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForANonHeader() {
        PulseList newPulses = new PulseList(this.newPulses, firstPulseLevel, 1);
        Collection<Byte> notHeaderData = Arrays.asList(
                (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        PZXDataBlock instance = new PZXDataBlock(newPulses, tailLength, numBitsInLastByte, notHeaderData);
        assertThat("Header flag is not set", instance.isHeader(), is(false));
    }

    /**
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeaderForAHeaderWithWrongLength() {
        PulseList newPulses = new PulseList(this.newPulses, firstPulseLevel, 1);
        Collection<Byte> notHeaderData = Arrays.asList(
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        PZXDataBlock instance =  new PZXDataBlock(newPulses, tailLength, numBitsInLastByte, notHeaderData);
        assertThat("Header flag is set but length is wrong", instance.isHeader(), is(false));
    }

    /**
     * Test of checkChecksum method, of class PZXDataBlock.
     */
    @Test
    public void testCheckChecksumForAValidChecksumBlock() {
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel, 1),
                                                 tailLength, numBitsInLastByte, data);
        assertThat(instance.checkChecksum(), is(true));
    }

    @Test
    public void testCheckChecksumForAnInvalidChecksumBlock() {
        Collection<Byte> badChecksum = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel, 1),
                                                 tailLength, numBitsInLastByte, badChecksum);
        assertThat(instance.checkChecksum(), is(false));
    }
    
}
