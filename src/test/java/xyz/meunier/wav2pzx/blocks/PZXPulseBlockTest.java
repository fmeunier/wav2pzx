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
import org.junit.Before;
import org.junit.Test;

import xyz.meunier.wav2pzx.PulseList;
import xyz.meunier.wav2pzx.blocks.PZXPulseBlock;

import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXPulseBlockTest {

    private static final PulseList PULSES = new PulseList(Arrays.asList(200.0, 200.0, 300.0), 1, 1);
	private PZXPulseBlock pulseBlock;
    
    public PZXPulseBlockTest() {
    }
    
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
        System.out.println("getPulses");
        Collection<Double> expResult = Arrays.asList(200.0, 200.0, 300.0);
        Collection<Double> result = pulseBlock.getPulses();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXPulseBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        System.out.println("getPZXBlockDiskRepresentation");
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
	 * Test method for {@link xyz.meunier.wav2pzx.blocks.PZXPulseBlock#equalWithinResoution(double, double, double)}.
	 */
    @Test
    public void testGetPZXBlockDiskRepresentation_PulseList() {
        System.out.println("getPZXBlockDiskRepresentation");
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
        System.out.println("getSummary");
        String expResult = "first pulse level: 1 pulse count: 3";
        String result = pulseBlock.getSummary();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class PZXPulseBlock.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "PZXPulseBlock [pulseList=PulseList [pulseLengths.size()=3, firstPulseLevel=1, resolution=1.0]]";
        String result = pulseBlock.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFirstPulseLevel method, of class PZXPulseBlock.
     */
    @Test
    public void testGetFirstPulseLevel() {
        System.out.println("getFirstPulseLevel");
        int expResult = 1;
        int result = pulseBlock.getFirstPulseLevel();
        assertEquals(expResult, result);
    }
    
}
