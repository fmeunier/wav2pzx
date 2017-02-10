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
package xyz.meunier.wav2pzx.input;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.meunier.wav2pzx.input.triggers.SchmittTrigger;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Fredrick Meunier
 */
public class AudioSamplePulseListBuilderTest {
    
    private static final double TOLERANCE = 0.001;
	// Two 238 t state pulses with a trailing 79 t state pulse
    private int[] samples1 = {0, 0, 0, 255, 255, 255, 0};
    private int[] samples2 = {255, 255, 255, 0, 0, 0, 0, 255};
    private static final float SAMPLE_RATE = (float) 44100.0;
    private static final float MACHINE_HZ = (float) 3500000.0;
    private AudioSamplePulseListBuilder instance1;
    private AudioSamplePulseListBuilder instance2;
    
    @Before
    public void setUp() {
        instance1 = new AudioSamplePulseListBuilder(SAMPLE_RATE, MACHINE_HZ, new SchmittTrigger());
        for(int i : samples1) {
            instance1.addSample(i);
        }
        
        // Test 2: Build samples2 should produce first pulse level 1
        instance2 = new AudioSamplePulseListBuilder(SAMPLE_RATE, MACHINE_HZ, new SchmittTrigger());
        for(int i : samples2) {
            instance2.addSample(i);
        }
    }
    
    @After
    public void tearDown() {
        instance1 = null;
        instance2 = null;
    }

    /**
     * Test of addSample method, of class AudioSamplePulseListBuilder.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddSampleThrowsExceptionForNegative() {
        // Test 1: shouldn't be able to add out of range samples
        instance1.addSample(-1);
    }

    /**
     * Test of addSample method, of class AudioSamplePulseListBuilder.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddSampleThrowsExceptionForLargeNum() {
        // Test 1: shouldn't be able to add out of range samples
        instance1.addSample(256);
    }

    /**
     * Test of addSample method, of class AudioSamplePulseListBuilder.
     */
    @Test(expected = IllegalStateException.class)
    public void testAddSampleCantAddSampleToACompletedList() {
        // Test 2: shouldn't be able to add a sample to a completed list
        instance1.build();
        instance1.addSample(0);
    }

    /**
     * Test of addSample method, of class AudioSamplePulseListBuilder.
     */
    @Test
    public void testAddSample() {
        // Test 3: Should be able to add an in-range sample to an incomplete list with no exception
        instance2.addSample(0);
    }

    /**
     * Test of getPulseList method, of class AudioSamplePulseListBuilder.
     */
    @Test
    public void testGetPulseList() {
        PulseList pulseList = instance1.build();

        assertThat(pulseList.getPulseLengths(), contains(238L, 238L, 79L));
    }

    /**
     * Test of firstPulseLevel method, of class AudioSamplePulseListBuilder.
     */
    @Test
    public void testFirstPulseLevel() {
        // Test 1: Build samples1 should produce first pulse level 0
        PulseList pulseList = instance1.build();
        assertThat(pulseList.getFirstPulseLevel(), is(0));
        
        // Test 2: Build samples2 should produce first pulse level 1
        pulseList = instance2.build();
        assertThat(pulseList.getFirstPulseLevel(), is(1));
    }

    /**
     * Test of firstPulseLevel method, of class AudioSamplePulseListBuilder.
     */
    @Test(expected = IllegalStateException.class)
    public void testBuildThrowsException() {
        // Test 0: check we get an exception when built with no pulses added
        AudioSamplePulseListBuilder instance = new AudioSamplePulseListBuilder(SAMPLE_RATE, MACHINE_HZ, new SchmittTrigger());
        instance.build();
    }

    /**
     * Test of firstPulseLevel method, of class AudioSamplePulseListBuilder.
     */
    @Test
    public void testBuild() {
        AudioSamplePulseListBuilder instance = new AudioSamplePulseListBuilder(SAMPLE_RATE, MACHINE_HZ, new SchmittTrigger());

        // Test that builder transitions to complete state when finished
        assertThat(instance.isTapeComplete(), is(false));

        // Test 1: Check we get a PulseList when build() is called and some samples have been received
        PulseList pulseList = instance1.build();
        assertThat(pulseList, is(notNullValue()));

        assertThat(instance1.isTapeComplete(), is(true));
        
        // Validate that final pulse is closed by this method?
        pulseList = instance2.build();
        assertThat(pulseList.getPulseLengths().size(), is(3));
    }
    
    /**
     * Test of getTStatesPerSample method, of class AudioSamplePulseListBuilder.
     */
    @Test
    public void testGetTStatesPerSample() {
        assertThat(instance1.getTStatesPerSample(), is(closeTo(79.365, TOLERANCE)));
    }

}
