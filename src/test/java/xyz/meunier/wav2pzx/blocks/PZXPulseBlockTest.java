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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.meunier.wav2pzx.PulseList;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXPulseBlockTest {

    private static final PulseList PULSES = new PulseList(Arrays.asList(200L, 200L, 300L), 1, 1);
	private PZXPulseBlock pulseBlock;
    
    @Before
    public void setUp() {
        this.pulseBlock = new PZXPulseBlock(PULSES);
    }
    
    @After
    public void tearDown() {
        this.pulseBlock = null;
    }

    /**
     * Test of getPulses method, of class PZXPulseBlock.
     */
    @Test
    public void testGetPulses() {
        assertThat(pulseBlock.getPulses(), equalTo(Arrays.asList(200L, 200L, 300L)));
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXPulseBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        byte[] expResult = {(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                            (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 8 bytes */
                            (byte)0x00, (byte)0x00, /* Initial pulse high */
                            (byte)0x02, (byte)0x80, /* Repeat count 2 */
                            (byte)0xc8, (byte)0x00, /* Pulse length 200 */
                            (byte)0x2c, (byte)0x01};/* Repeat count 1, pulse length 300 */
        byte[] result = pulseBlock.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result); 
    }

	/**
	 * Test method for {@link xyz.meunier.wav2pzx.blocks.PZXPulseBlock#getPZXBlockDiskRepresentation(PulseList)}.
	 */
    @Test
    public void testGetPZXBlockDiskRepresentation_PulseList() {
        byte[] expResult = {(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                            (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 8 bytes */
                            (byte)0x00, (byte)0x00, /* Initial pulse high */
                            (byte)0x02, (byte)0x80, /* Repeat count 2 */
                            (byte)0xc8, (byte)0x00, /* Pulse length 200 */
                            (byte)0x2c, (byte)0x01};/* Repeat count 1, pulse length 300 */
        byte[] result = PZXPulseBlock.getPZXBlockDiskRepresentation(PULSES);
        assertArrayEquals(expResult, result); 
    }

    /**
     * Test of getSummary method, of class PZXPulseBlock.
     */
    @Test
    public void testGetSummary() {
        assertThat(pulseBlock.getSummary(), is("PZXPulseBlock: first pulse level: 1 pulse count: 3"));
    }

    /**
     * Test of toString method, of class PZXPulseBlock.
     */
    @Test
    public void testToString() {
        assertThat(pulseBlock.toString(),
                   is("PZXPulseBlock [pulseList=PulseList [pulseLengths.size()=3, firstPulseLevel=1, resolution=1]]"));
    }

    /**
     * Test of getFirstPulseLevel method, of class PZXPulseBlock.
     */
    @Test
    public void testGetFirstPulseLevel() {
        assertThat(pulseBlock.getFirstPulseLevel(), is(1));
    }
    
}
