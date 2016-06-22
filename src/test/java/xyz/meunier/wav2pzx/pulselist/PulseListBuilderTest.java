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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PulseListBuilderTest {

    private PulseListBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new PulseListBuilder();
    }

    @Test
    public void with0FirstPulseLevel() throws Exception {
        builder.withFirstPulseLevel(0);
        assertThat(builder.getFirstPulseLevel(), is(0));
    }

    @Test
    public void with1FirstPulseLevel() throws Exception {
        builder.withFirstPulseLevel(1);
        assertThat(builder.getFirstPulseLevel(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withIllegalFirstPulseLevel() throws Exception {
        builder.withFirstPulseLevel(10);
    }

    @Test
    public void withResolution() throws Exception {
        builder.withResolution(1);
        assertThat(builder.getResolution(), is(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withIllegalResolution() throws Exception {
        builder.withResolution(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullNextPulse() throws Exception {
        builder.withNextPulse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNegativeNextPulse() throws Exception {
        builder.withNextPulse(-1L);
    }

    @Test
    public void build() throws Exception {
        // Test the remaining methods work normally in the happy case
        PulseList pulseList = builder.withNextPulse(1L)
                .withNextPulses(asList(2L, 3L))
                .withFirstPulseLevel(0)
                .withResolution(1)
                .build();

        assertThat(pulseList, is(notNullValue()));
        assertThat(pulseList.getFirstPulseLevel(), is(0));
        assertThat(pulseList.getResolution(), is(1L));
        assertThat(pulseList.getPulseLengths(), is(asList(1L, 2L, 3L)));
    }

    @Test
    public void isEmpty() throws Exception {
        assertThat(builder.isEmpty(), is(true));
        builder.withNextPulse(1L);
        assertThat(builder.isEmpty(), is(false));
    }

}