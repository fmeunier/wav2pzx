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
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xyz.meunier.wav2pzx.blocks.PZXHeaderBlock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PZXHeaderBlockTest {

    private PZXHeaderBlock instance;

    @Before
    public void setUp() throws Exception {
        instance = new PZXHeaderBlock();
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXHeaderBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        byte[] expResult = {(byte)0x50, (byte)0x5a, (byte)0x58, (byte)0x54, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00};
        byte[] result = instance.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getSummary method, of class PZXHeaderBlock.
     */
    @Test
    public void testGetSummary() {
        assertThat(instance.getSummary(), equalTo("PZXHeaderBlock: PZX major version: 1 minor version: 0"));
    }

    /**
     * Test of getPulses method, of class PZXHeaderBlock.
     */
    @Test
    public void testGetPulses() {
        assertThat(instance.getPulses(), equalTo(new ArrayList<Long>()));
    }

    /**
     * Test of toString method, of class PZXHeaderBlock.
     */
    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("PZXHeaderBlock{PZX major version: 1 minor version: 0}"));
    }
    
}
