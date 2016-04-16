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

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXEncodeUtilsTest {
    
    /**
     * Test of addPZXBlockHeader method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddPZXBlockHeader_String_Collection() {
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
        int outputVal = 0xdeadbeef;
        Collection<Byte> output = new ArrayList<>();
        List<Byte> expectedResult = Arrays.asList((byte)0xef, (byte)0xbe, (byte)0xad, (byte)0xde);
        PZXEncodeUtils.putUnsignedLittleEndianInt(outputVal, output);
        assertThat("Setting int", output, equalTo(expectedResult));
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
        assertThat("Setting short", output, equalTo(expectedResult));
    }

    /**
     * Test of putUnsignedByte method, of class PZXEncodeUtils.
     */
    @Test
    public void testPutUnsignedByte() {
        byte outputVal = 20;
        Collection<Byte> output = new ArrayList<>();
        List<Byte> expectedResult = Collections.singletonList((byte)20);
        PZXEncodeUtils.putUnsignedByte(outputVal, output);
        assertThat("Setting byte", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForWithOneCycleShortPulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test one cycle short pulse <= 0x7fff
        PZXEncodeUtils.addBytesFor(0x7000L, 1, output);
        List<Byte> expectedResult = Arrays.asList((byte) 0x00, (byte) 0x70);
        assertThat("Check one cycle short pulse", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForWithOnceCycleLongerPulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test one cycle longer pulse >= 0x8000 < 0x7fffff
        PZXEncodeUtils.addBytesFor(0x8100L, 1, output);
        List<Byte> expectedResult = Arrays.asList((byte) 0x01, (byte) 0x80, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x81);
        assertThat("Check one cycle pulse >= 0x8000 < 0x7fffff", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForShortMultiCyclePulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test multi cycle pulse <= 0x7fff
        PZXEncodeUtils.addBytesFor(0x7000L, 2, output);
        List<Byte> expectedResult = Arrays.asList((byte) 0x02, (byte) 0x80, (byte) 0x00, (byte) 0x70);
        assertThat("Check multi cycle pulse <= 0x7fff cycles", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForLongerMultiCyclePulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test multi cycle pulse >= 0x8000 < 0x80000
        PZXEncodeUtils.addBytesFor(0x8100L, 2, output);
        List<Byte> expectedResult = Arrays.asList((byte) 0x02, (byte) 0x80, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x81);
        assertThat("Check multi cycle pulse >= 0x8000 < 0x80000", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForEvenLongerMultiCyclePulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test multi cycle pulse >= 0x80000 < 0x7fffff
        PZXEncodeUtils.addBytesFor(0x81000L, 2, output);
        List<Byte> expectedResult = Arrays.asList((byte) 0x02, (byte) 0x80, (byte) 0x08, (byte) 0x80, (byte) 0x00, (byte) 0x10);
        assertThat("Check multi cycle pulse >= 0x80000 < 0x7fffff", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForVeryLongMultiCyclePulse() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test one cycle very long pulse that is multiple of a 0x7fffffff
        PZXEncodeUtils.addBytesFor(0x7fffffffL * 2, 1, output);
        List<Byte> expectedResult = Arrays.asList(
                (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff);
        assertThat("Check one cycle very long pulse that is a multiple of 0x7fffffff", output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForOneVeryLongPulseWithShortRemainder() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test one cycle very long pulse that is not a multiple of 0x7fffffff
        // Note: there are short and long last encoded cycle versions of this
        // Short
        PZXEncodeUtils.addBytesFor(0x7fffffffL * 2 + 1L, 1, output);
        List<Byte> expectedResult = Arrays.asList(
                (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00,
                (byte) 0x00, (byte) 0x01, (byte) 0x00);
        assertThat("Check one cycle very long pulse that is not a multiple of 0x7fffffff, short remainder",
                output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForOneVeryLongPulseWithLongRemainder() {
        ArrayList<Byte> output = new ArrayList<>();

        // Long
        PZXEncodeUtils.addBytesFor(0x7fffffffL * 2 + 0x8100L, 1, output);
        List<Byte> expectedResult = Arrays.asList(
                (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00,
                (byte) 0x00, (byte) 0x01, (byte) 0x80, (byte) 0x00, (byte) 0x80,
                (byte) 0x00, (byte) 0x81);
        assertThat("Check one cycle very long pulse that is not a multiple of 0x7fffffff, long remainder",
                output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForOneVeryLongPulseWithNoRemainder() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test multi cycle very long pulse that is a multiple of 0x7fffffff
        PZXEncodeUtils.addBytesFor(0x7fffffffL * 2, 2, output);
        List<Byte> expectedResult = Arrays.asList(
                (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff);
        assertThat("Check multi cycle very long pulse that is a multiple of 0x7fffffff",
                output, equalTo(expectedResult));
    }

    /**
     * Test of addBytesFor method, of class PZXEncodeUtils.
     */
    @Test
    public void testAddBytesForOneVeryLongPulseWithARemainder() {
        ArrayList<Byte> output = new ArrayList<>();

        // Test multi cycle very long pulse that is not a multiple of 0x7fffffff
        PZXEncodeUtils.addBytesFor(0x7fffffffL * 2 + 1L, 2, output);
        List<Byte> expectedResult = Arrays.asList(
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x00,
                (byte)0x01, (byte)0x80, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x80, 
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, 
                (byte)0x00, (byte)0x01, (byte)0x00);
        assertThat("Check multi cycle very long pulse that is not a multiple of 0x7fffffff",
                   output, equalTo(expectedResult));
    }

}
