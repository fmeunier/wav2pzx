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

import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXDataBlockTest {
    
    int firstPulseLevel = 1;
    Collection<Double> newPulses = Arrays.asList(200.0, 200.0);
    double tailLength = 945.0;
    int numBitsInLastByte = 8;
    Collection<Byte> data = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x30);
                                
    public PZXDataBlockTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXDataBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        System.out.println("getPZXBlockDiskRepresentation");
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel),
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
     * Test of isHeader method, of class PZXDataBlock.
     */
    @Test
    public void testIsHeader() {
        System.out.println("isHeader");
        PulseList newPulses2 = new PulseList(newPulses, firstPulseLevel);
		PZXDataBlock instance =  new PZXDataBlock(newPulses2, tailLength, numBitsInLastByte, data);
        boolean expResult = false;
        boolean result = instance.isHeader();
        assertEquals("Block too short to be a header", expResult, result);

        Collection<Byte> headerData = Arrays.asList(
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        instance =  new PZXDataBlock(newPulses2, tailLength, numBitsInLastByte, headerData);
        expResult = true;
        result = instance.isHeader();
        assertEquals("Block is a header", expResult, result);

        Collection<Byte> notHeaderData = Arrays.asList(
                (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        instance =  new PZXDataBlock(newPulses2,
                                     tailLength, numBitsInLastByte, notHeaderData);
        expResult = false;
        result = instance.isHeader();
        assertEquals("Header flag is not set", expResult, result);

        notHeaderData = Arrays.asList(
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        instance =  new PZXDataBlock(newPulses2,
                                     tailLength, numBitsInLastByte, notHeaderData);
        expResult = false;
        result = instance.isHeader();
        assertEquals("Header flag is set but length is wrong", expResult, result);

    }

    /**
     * Test of checkChecksum method, of class PZXDataBlock.
     */
    @Test
    public void testCheckChecksum() {
        System.out.println("checkChecksum");
        PZXDataBlock instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel),
                                                    tailLength, numBitsInLastByte, data);
        boolean expResult = true;
        boolean result = instance.checkChecksum();
        assertEquals(expResult, result);
        
        Collection<Byte> badChecksum = Arrays.asList((byte)0x10, (byte)0x20, (byte)0x33);
        instance = new PZXDataBlock(new PulseList(newPulses, firstPulseLevel),
                                    tailLength, numBitsInLastByte, badChecksum);
        expResult = false;
        result = instance.checkChecksum();
        assertEquals(expResult, result);
    }
    
}
