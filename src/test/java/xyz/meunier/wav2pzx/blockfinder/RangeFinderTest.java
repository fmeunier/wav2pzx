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

package xyz.meunier.wav2pzx.blockfinder;

import org.junit.Test;

import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.singleton;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static xyz.meunier.wav2pzx.blockfinder.RangeFinder.getRanges;

public class RangeFinderTest {

    @Test(expected = NullPointerException.class)
    public void shouldGetNullPointerExceptionWithNullArgument() throws Exception {
        getRanges(null);
    }

    @Test
    public void shouldGetEmptyListWithEmptyListArgument() throws Exception {
        assertThat(getRanges(emptyList()), is(emptyList()));
    }

    @Test
    public void shouldGetSingleRangeListWithSingleEntryArgument() throws Exception {
        assertThat(getRanges(singletonList(3100L)), is(singletonList(singleton(3100L))));
    }

    @Test
    public void shouldExtendRangeBy50OnEachSide() throws Exception {
        assertThat(getRanges(asList(3100L, 3200L)), is(singletonList(closed(3050L, 3250L))));
    }

    @Test
    public void shouldSplitRangeWithASignificantGap() throws Exception {
        assertThat(getRanges(asList(1000L, 1100L, 3100L, 3200L)), is(asList(closed(950L, 1150L), closed(3050L, 3250L))));
    }
}