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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xyz.meunier.wav2pzx.blocks.PZXNullBlock;

public class PZXNullBlockTest {

	private PZXNullBlock instance;
	
	@Before
	public void setUp() throws Exception {
        this.instance = new PZXNullBlock();
	}

	@After
	public void tearDown() throws Exception {
		this.instance = null;
	}

    /**
     * Test of getPZXBlockDiskRepresentation method, of class PZXNullBlock.
     */
    @Test
    public void testGetPZXBlockDiskRepresentation() {
        System.out.println("getPZXBlockDiskRepresentation");
        byte[] expResult = {};
        byte[] result = instance.getPZXBlockDiskRepresentation();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getSummary method, of class PZXNullBlock.
     */
    @Test
    public void testGetSummary() {
        System.out.println("getSummary");
        String expResult = "Null PZX block";
        String result = instance.getSummary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPulses method, of class PZXNullBlock.
     */
    @Test
    public void testGetPulses() {
        System.out.println("getPulses");
        assertTrue(instance.getPulses().isEmpty());
	}

    /**
     * Test of getFirstPulseLevel method, of class PZXNullBlock.
     */
	@Test
	public void testGetFirstPulseLevel() {
        System.out.println("getFirstPulseLevel");
        assertEquals(0, instance.getFirstPulseLevel());
	}
}
