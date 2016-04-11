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
import xyz.meunier.wav2pzx.blocks.PZXPilotBlock;

import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXPilotBlockTest {

    private PZXPilotBlock instance;
    private int firstPulseLevel = 1;
    private long sync1Length = 667;
    private long sync2Length = 735;
    private Collection<Long> newPulses = Arrays.asList(2168L, 2168L, sync1Length, sync2Length);
    
    public PZXPilotBlockTest() {
    }
    
    @Before
    public void setUp() {
        this.instance = new PZXPilotBlock(new PulseList(this.newPulses, this.firstPulseLevel, 1));
    }
    
    @After
    public void tearDown() {
        this.instance = null;
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXPilotBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        System.out.println("getPZXBlockDiskRepresentation");
        byte[] expResult = {(byte)80, (byte)85, (byte)76, (byte)83, /* PULS */
                            (byte)0x0a, (byte)0x00, (byte)0x00, (byte)0x00, /* Length: 10 bytes */
                            (byte)0x00, (byte)0x00, /* Initial pulse high */
                            (byte)0x02, (byte)0x80, /* Repeat count 2 */
                            (byte)0x78, (byte)0x08, /* Pulse length 2168 */
                            (byte)0x9b, (byte)0x02, /* Repeat count 1, pulse length 667 */
                            (byte)0xdf, (byte)0x02};/* Repeat count 1, pulse length 735 */
        byte[] result = instance.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toString method, of class PZXPilotBlock.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "PZXPilotBlock{PulseList [pulseLengths.size()=4, firstPulseLevel=1, resolution=1], sync1Length=667, sync2Length=735}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
