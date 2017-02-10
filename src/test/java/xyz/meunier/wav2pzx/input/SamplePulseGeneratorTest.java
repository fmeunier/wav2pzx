/*
 * Copyright (c) 2017, Fredrick Meunier
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
package xyz.meunier.wav2pzx.input;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.meunier.wav2pzx.input.triggers.SchmittTrigger;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class SamplePulseGeneratorTest {

    // Two 238 t state pulses with a trailing 79 t state pulse
    private int[] samples1 = {0, 0, 0, 1, 1, 1, 0};
    private int[] samples2 = {1, 1, 1, 0, 0, 0, 0, 1};
    private SamplePulseGenerator instance1;
    private SamplePulseGenerator instance2;

    @Before
    public void setUp() {
        instance1 = new SamplePulseGenerator();
        for(int i : samples1) {
            instance1.addSample( i,79 );
        }

        // Test 2: Build samples2 should produce first pulse level 1
        instance2 = new SamplePulseGenerator();
        for(int i : samples2) {
            instance2.addSample( i, 79 );
        }
    }

    @After
    public void tearDown() {
        instance1 = null;
        instance2 = null;
    }

    @Test
    public void shouldGetExpectedPulseList() throws Exception {
        SamplePulseGenerator samplePulseGenerator = new SamplePulseGenerator();

        samplePulseGenerator.addSample(0, 100 );
        samplePulseGenerator.addSample( 0, 100 );
        samplePulseGenerator.addSample( 1, 100 );

        PulseList pulseList = samplePulseGenerator.build();

        assertThat(pulseList.getPulseLengths(), contains(200L, 100L));
        assertThat(pulseList.getFirstPulseLevel(), is(0));
    }

    /**
     * Test of addSample method, of class SamplePulseGenerator.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddSampleThrowsExceptionForNegative() {
        // Test 1: shouldn't be able to add out of range samples
        instance1.addSample(-1, 79 );
    }

    /**
     * Test of addSample method, of class SamplePulseGenerator.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddSampleThrowsExceptionForLargeNum() {
        // Test 1: shouldn't be able to add out of range samples
        instance1.addSample(2, 79 );
    }

    /**
     * Test of addSample method, of class SamplePulseGenerator.
     */
    @Test(expected = IllegalStateException.class)
    public void testAddSampleCantAddSampleToACompletedList() {
        // Test 2: shouldn't be able to add a sample to a completed list
        instance1.build();
        instance1.addSample(0, 79 );
    }

    /**
     * Test of addSample method, of class SamplePulseGenerator.
     */
    @Test
    public void testAddSample() {
        // Test 3: Should be able to add an in-range sample to an incomplete list with no exception
        instance2.addSample(0, 79 );
    }

    /**
     * Test of getPulseList method, of class SamplePulseGenerator.
     */
    @Test
    public void testGetPulseList() {
        PulseList pulseList = instance1.build();

        assertThat(pulseList.getPulseLengths(), contains(237L, 237L, 79L));
    }

    /**
     * Test of firstPulseLevel method, of class SamplePulseGenerator.
     */
    @Test
    public void testFirstPulseLevel() {
        // Test 1: Build samples1 should produce first pulse level 0
        PulseList pulseList = instance1.build();
        assertThat(pulseList.getFirstPulseLevel(), Matchers.is(0));

        // Test 2: Build samples2 should produce first pulse level 1
        pulseList = instance2.build();
        assertThat(pulseList.getFirstPulseLevel(), Matchers.is(1));
    }

    /**
     * Test of firstPulseLevel method, of class SamplePulseGenerator.
     */
    @Test(expected = IllegalStateException.class)
    public void testBuildThrowsException() {
        // Test 0: check we get an exception when built with no pulses added
        SamplePulseGenerator instance = new SamplePulseGenerator();
        instance.build();
    }

    /**
     * Test of firstPulseLevel method, of class SamplePulseGenerator.
     */
    @Test
    public void testBuild() {
        SamplePulseGenerator instance = new SamplePulseGenerator();

        // Test that builder transitions to complete state when finished
        assertThat(instance.isTapeComplete(), Matchers.is(false));

        // Test 1: Check we get a PulseList when build() is called and some samples have been received
        PulseList pulseList = instance1.build();
        assertThat(pulseList, Matchers.is(notNullValue()));

        assertThat(instance1.isTapeComplete(), Matchers.is(true));

        // Validate that final pulse is closed by this method?
        pulseList = instance2.build();
        assertThat(pulseList.getPulseLengths().size(), Matchers.is(3));
    }

}