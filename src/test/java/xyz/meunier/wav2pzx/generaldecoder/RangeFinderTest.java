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

package xyz.meunier.wav2pzx.generaldecoder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.singleton;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static xyz.meunier.wav2pzx.generaldecoder.RangeFinder.*;

public class RangeFinderTest {

    private final List<Long> MULTI_SYMBOL_PULSE_LIST = asList(1000L, 1100L, 3100L, 3200L);
    private final List<Range<Long>> MULTI_SYMBOL_RANGES = asList(closed(950L, 1150L), closed(3050L, 3250L));

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
        assertThat(getRanges(MULTI_SYMBOL_PULSE_LIST), is(MULTI_SYMBOL_RANGES));
    }

    @Test
    public void shouldGetAveragePulseCalculatedPerRange() throws Exception {
        List<BitData> calculatedData = getReplacementBitDataOfRanges(MULTI_SYMBOL_RANGES, MULTI_SYMBOL_PULSE_LIST);

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), 1050L));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), 3150L));

        assertThat(calculatedData, is(expectedData));
    }

    @Test
    public void shouldGetAZeroAverageIfNoPulsesMatchSuppliedRange() throws Exception {
        List<BitData> actualMap = getReplacementBitDataOfRanges(MULTI_SYMBOL_RANGES, emptyList());

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), 0L));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), 0L));

        assertThat(actualMap, is(expectedData));
    }

    @Test
    public void shouldGetOneRangeForEachSourcePulse() throws Exception {
        List<Range<Long>> expectedList = asList(singleton(1000L), singleton(1100L), singleton(3100L), singleton(3200L));
        assertThat(getRangesForSinglePulses(MULTI_SYMBOL_PULSE_LIST), is(equalTo(expectedList)));
    }

    @Test
    public void shouldGetSuitableRangesForSingletonPulses() throws Exception {
        List<Range<Long>> expectedList = asList(singleton(1000L), singleton(1100L), singleton(3100L), singleton(3200L));

        List<BitData> expectedMap = ImmutableList.of(
                new BitData(singleton(1000L), 1000L),
                new BitData(singleton(1100L), 1100L),
                new BitData(singleton(3100L), 3100L),
                new BitData(singleton(3200L), 3200L)
        );

        assertThat(getSingletonPulseLengthsOfRanges(expectedList), is(equalTo(expectedMap)));
    }

}