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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

import xyz.meunier.wav2pzx.blocks.PZXEncodeUtils;

import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXEncodeUtilsTest {
    
    public PZXEncodeUtilsTest() {
    }

    /**
     * Test of addPZXBlockHeader method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddPZXBlockHeader_String_Collection() {
        System.out.println("addPZXBlockHeader");
        String headerTag = "TEST";
        Collection<Byte> output = Arrays.asList((byte)0x20, (byte)0x30);
        byte[] expResult = {(byte)84, (byte)69, (byte)83, (byte)84, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x30};
        byte[] result = PZXEncodeUtils.addPZXBlockHeader(headerTag, output);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of addPZXBlockHeader method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddPZXBlockHeader_String_byteArr() {
        System.out.println("addPZXBlockHeader");
        String headerTag = "TEST";
        byte[] output = {(byte)0x20, (byte)0x30};
        byte[] expResult = {(byte)84, (byte)69, (byte)83, (byte)84, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x30};
        byte[] result = PZXEncodeUtils.addPZXBlockHeader(headerTag, output);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of putUnsignedLittleEndianInt method, of class PZXEncodeUtils.
     */
    @Test
    public void testPutUnsignedLittleEndianInt() {
        System.out.println("putUnsignedLittleEndianInt");
        int outputVal = 0xdeadbeef;
        Collection<Byte> output = new ArrayList<>();
        List<Byte> expectedResult = Arrays.asList((byte)0xef, (byte)0xbe, (byte)0xad, (byte)0xde);
        PZXEncodeUtils.putUnsignedLittleEndianInt(outputVal, output);
        assertArrayEquals("Setting int", expectedResult.toArray(), output.toArray());
    }

    /**
     * Test of putUnsignedLittleEndianShort method, of class PZXEncodeUtils.
     */
    @Test
    public void testPutUnsignedLittleEndianShort() {
        System.out.println("putUnsignedLittleEndianShort");
        short outputVal = (short)0xdead;
        Collection<Byte> output = new ArrayList<>();
        List<Byte> expectedResult = Arrays.asList((byte)0xad, (byte)0xde);
        PZXEncodeUtils.putUnsignedLittleEndianShort(outputVal, output);
        assertArrayEquals("Setting short", expectedResult.toArray(), output.toArray());
    }

    /**
     * Test of putUnsignedByte method, of class PZXEncodeUtils.
     */
    @Test
    public void testPutUnsignedByte() {
        System.out.println("putUnsignedByte");
        byte outputVal = 20;
        Collection<Byte> output = new ArrayList<>();
        List<Byte> expectedResult = Arrays.asList((byte)20);
        PZXEncodeUtils.putUnsignedByte(outputVal, output);
        assertArrayEquals("Setting byte", expectedResult.toArray(), output.toArray());
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesFor() {
        System.out.println("addBytesFor");
        long pulse;
        ArrayList<Byte> output = new ArrayList<>();
        List<Byte> expectedResult;
        
        // Test one cycle short pulse <= 0x7fff
        output.clear();
        pulse = 0x7000L;
        PZXEncodeUtils.addBytesFor(pulse, 1, output);
        expectedResult = Arrays.asList((byte)0x00, (byte)0x70);
        assertTrue("Check one cycle short pulse", Arrays.equals(expectedResult.toArray(), output.toArray()));
        
        // Test one cycle longer pulse >= 0x8000 < 0x7fffff
        output.clear();
        pulse = 0x8100L;
        PZXEncodeUtils.addBytesFor(pulse, 1, output);
        expectedResult = Arrays.asList((byte)0x01, (byte)0x80, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x81);
        assertTrue("Check one cycle pulse >= 0x8000 < 0x7fffff", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test multi cycle pulse <= 0x7fff
        output.clear();
        pulse = 0x7000L;
        PZXEncodeUtils.addBytesFor(pulse, 2, output);
        expectedResult = Arrays.asList((byte)0x02, (byte)0x80, (byte)0x00, (byte)0x70);
        assertTrue("Check multi cycle pulse <= 0x7fff cycles", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test multi cycle pulse >= 0x8000 < 0x80000
        output.clear();
        pulse = 0x8100L;
        PZXEncodeUtils.addBytesFor(pulse, 2, output);
        expectedResult = Arrays.asList((byte)0x02, (byte)0x80, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x81);
        assertTrue("Check multi cycle pulse >= 0x8000 < 0x80000", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test multi cycle pulse >= 0x80000 < 0x7fffff
        output.clear();
        pulse = 0x81000L;
        PZXEncodeUtils.addBytesFor(pulse, 2, output);
        expectedResult = Arrays.asList((byte)0x02, (byte)0x80, (byte)0x08, (byte)0x80, (byte)0x00, (byte)0x10);
        assertTrue("Check multi cycle pulse >= 0x80000 < 0x7fffff", Arrays.equals(expectedResult.toArray(), output.toArray()));
        
        // Test one cycle very long pulse that is multiple of a 0x7fffffff
        output.clear();
        pulse = 0x7fffffffL * 2;
        PZXEncodeUtils.addBytesFor(pulse, 1, output);
        expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff);
        assertTrue("Check one cycle very long pulse that is a multiple of 0x7fffffff", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test one cycle very long pulse that is not a multiple of 0x7fffffff
        // Note: there are short and long last encoded cycle versions of this
        // Short
        output.clear();
        pulse = 0x7fffffffL * 2 + 1L;
        PZXEncodeUtils.addBytesFor(pulse, 1, output);
        expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x00);
        assertTrue("Check one cycle very long pulse that is not a multiple of 0x7fffffff, short remainder", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Long
        output.clear();
        pulse = 0x7fffffffL * 2 + 0x8100L;
        PZXEncodeUtils.addBytesFor(pulse, 1, output);
        expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x80, (byte)0x00, (byte)0x80, 
                (byte)0x00, (byte)0x81);
        assertTrue("Check one cycle very long pulse that is not a multiple of 0x7fffffff, long remainder", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test multi cycle very long pulse that is a multiple of 0x7fffffff
        output.clear();
        pulse = 0x7fffffffL * 2;
        PZXEncodeUtils.addBytesFor(pulse, 2, output);
        expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff);
        assertTrue("Check multi cycle very long pulse that is a multiple of 0x7fffffff", Arrays.equals(expectedResult.toArray(), output.toArray()));

        // Test multi cycle very long pulse that is not a multiple of 0x7fffffff
        output.clear();
        pulse = 0x7fffffffL * 2 + 1L;
        PZXEncodeUtils.addBytesFor(pulse, 2, output);
        expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x00,
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x00);
        assertTrue("Check multi cycle very long pulse that is not a multiple of 0x7fffffff", Arrays.equals(expectedResult.toArray(), output.toArray()));

    }

}
