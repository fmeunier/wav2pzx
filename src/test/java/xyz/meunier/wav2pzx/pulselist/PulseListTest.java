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
package xyz.meunier.wav2pzx.pulselist;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Fredrick Meunier
 */
public class PulseListTest {

    private final List<Long> pulses = Arrays.asList(200L, 300L);
    private PulseList instance;

    public PulseListTest() {
    }

    @Before
    public void setup() {
        instance = new PulseList(pulses, 1, 1);
    }

    @Test
    public void testPulseListConcatenationConstructor() {
        PulseList instance2 = new PulseList(singletonList(100L), 1, 1);
        PulseList instance3 = new PulseList(instance, instance2);

        assertThat(instance3.getPulseLengths(), is(Arrays.asList(200L, 300L, 100L)));
    }

    /**
     * Test of getPulseLengths method, of class PulseList.
     */
    @Test
    public void testGetPulseLengths() {
        assertThat(instance.getPulseLengths(), contains(Arrays.asList(equalTo(200L), equalTo(300L))));
    }

    /**
     * Test of getFirstPulseLevel method, of class PulseList.
     */
    @Test
    public void testGetFirstPulseLevel() {
        assertThat(instance.getFirstPulseLevel(), is(1));

        instance = new PulseList(pulses, 0, 1);
        assertThat(instance.getFirstPulseLevel(), is(0));
    }

    /**
     * Test of getPulseLengths method, of class PulseList.
     */
    @Test
    public void testToPulseListText() {
        assertThat(instance.toPulseListText(), is("PULSES\nPULSE 200\nPULSE 300\n"));
    }
}
