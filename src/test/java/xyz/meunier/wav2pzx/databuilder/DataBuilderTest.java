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

package xyz.meunier.wav2pzx.databuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DataBuilderTest {
    private DataBuilder instance;

    @Before
    public void setUp() throws Exception {
        instance = new DataBuilder();
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    @Test
    public void getData() throws Exception {
        // add 16 more bits to add two entries to data collection
        addBits(16);

        assertThat("Check data was generated", instance.getData(), equalTo(Arrays.asList((byte) 0xff, (byte) 0xff)));
    }

    @Test
    public void getNumBitsInCurrentByte() throws Exception {
        assertThat("Check number of bits in current byte is 0", instance.getNumBitsInCurrentByte(), is(0));
        instance.addBit(1);
        assertThat("Check number of bits in current byte is 1", instance.getNumBitsInCurrentByte(), is(1));

        // add 7 more bits to add an entry to data collection
        addBits(7);

        assertThat("Check number of bits in current byte is 8", instance.getNumBitsInCurrentByte(), is(8));
    }

    @Test
    public void addBit() throws Exception {

        instance.addBit(1);

        assertThat("Check bit has been handled", instance.getCurrentByte(), is((byte)0x01));

        // add 7 more bits to add an entry to data collection
        addBits(7);

        assertThat("Check byte was generated", instance.getData(), equalTo(Collections.singletonList((byte)0xff)));
    }

    @Test
    public void getCurrentByte() throws Exception {

        assertThat("Initial state should be 0", instance.getCurrentByte(), is((byte)0x00));

        instance.addBit(1);

        assertThat("Check bit has been handled", instance.getCurrentByte(), is((byte)0x01));

        // add 6 more bits to add an entry to data collection
        addBits(6);

        assertThat("Check bits have been handled", instance.getCurrentByte(), is((byte)0x7f));

        instance.addBit(1);

        assertThat("State should return to 0", instance.getCurrentByte(), is((byte)0x00));
    }

    private void addBits(int numBits) {
        for(int i = 0; i < numBits; i++) {
            instance.addBit(1);
        }
    }
}
