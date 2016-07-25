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

    private final List<Long> SINGLE_SYMBOL_PULSE_LIST = asList(1000L, 1100L);
    private final List<Range<Long>> SINGLE_SYMBOL_RANGES = singletonList(closed(950L, 1150L));
    private final List<Long> DUAL_SYMBOL_PULSE_LIST = asList(1000L, 1100L, 3100L, 3200L);
    private final List<Range<Long>> DUAL_SYMBOL_RANGES = asList(closed(950L, 1150L), closed(3050L, 3250L));
    private final List<Long> TRIPLE_SYMBOL_PULSE_LIST = asList(1000L, 1100L, 2000L, 2100L, 3100L, 3200L);
    private final List<Range<Long>> TRIPLE_SYMBOL_RANGES =
            asList(closed(950L, 1150L), closed(1950L, 2150L), closed(3050L, 3250L));
    // Focus on the case I have in BCs Quest for tires
    private final List<Long> BCS_QUEST_PULSES = asList(
            1659L, 1659L, 1501L, 1501L, 1659L, 1659L, 1659L, 1659L, 1659L, 1659L, 1659L, 1501L, 1580L, 1580L,
            1580L, 1580L, 1580L, 1580L, 1580L, 1580L, 2212L, 2370L, 1738L, 2212L, 2370L, 1738L, 2212L, 2370L,
            1738L, 1580L, 1580L, 2212L, 2370L, 1738L, 2212L, 1738L, 2212L, 2370L, 2370L, 1738L
    );
    private List<Range<Long>> BCS_QUEST_RANGES = asList(closed(1500L, 1800L), closed(2200L, 2400L));

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
        assertThat(getRanges(DUAL_SYMBOL_PULSE_LIST), is(DUAL_SYMBOL_RANGES));
    }

    @Test
    public void shouldGetAveragePulseCalculatedPerRange() throws Exception {
        List<BitData> calculatedData = getReplacementBitDataOfRanges(DUAL_SYMBOL_RANGES, DUAL_SYMBOL_PULSE_LIST);

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), singletonList(1050L)));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), singletonList(3150L)));

        assertThat(calculatedData, is(expectedData));
    }

    @Test
    public void shouldGetAZeroAverageIfNoPulsesMatchSuppliedRange() throws Exception {
        List<BitData> actualMap = getReplacementBitDataOfRanges(DUAL_SYMBOL_RANGES, emptyList());

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), singletonList(0L)));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), singletonList(0L)));

        assertThat(actualMap, is(expectedData));
    }

    @Test
    public void shouldGetOneRangeForEachSourcePulse() throws Exception {
        List<Range<Long>> expectedList = asList(singleton(1000L), singleton(1100L), singleton(3100L), singleton(3200L));
        assertThat(getRangesForSinglePulses(DUAL_SYMBOL_PULSE_LIST), is(equalTo(expectedList)));
    }

    @Test
    public void shouldGetSuitableRangesForSingletonPulses() throws Exception {
        List<Range<Long>> expectedList = asList(singleton(1000L), singleton(1100L), singleton(3100L), singleton(3200L));

        List<BitData> expectedBitDataList = ImmutableList.of(
                new BitData(singleton(1000L), singletonList(1000L)),
                new BitData(singleton(1100L), singletonList(1100L)),
                new BitData(singleton(3100L), singletonList(3100L)),
                new BitData(singleton(3200L), singletonList(3200L))
        );

        assertThat(getSingletonPulseLengthsOfRanges(expectedList), is(equalTo(expectedBitDataList)));
    }

    // TODO: Test assymmetric pulse handling in getZeroAndOnePulsePairs()
    // Collection of 1 or 3 range pulses should come back as averages for symmetric pulses
    @Test
    public void shouldGetAverageSymmetricPulsesFromOneRangePulseList() throws Exception {
        List<BitData> pulsePairs = getZeroAndOnePulsePairs(SINGLE_SYMBOL_RANGES, SINGLE_SYMBOL_PULSE_LIST);

        List<BitData> expectedBitDataList = ImmutableList.of(
                new BitData(SINGLE_SYMBOL_RANGES.get(0), singletonList(1050L))
        );

        assertThat(pulsePairs, is(equalTo(expectedBitDataList)));
    }

    @Test
    public void shouldGetAverageSymmetricPulsesFromThreeRangePulseList() {
        List<BitData> pulsePairs = getZeroAndOnePulsePairs(TRIPLE_SYMBOL_RANGES, TRIPLE_SYMBOL_PULSE_LIST);

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), singletonList(1050L)));
        expectedData.add(new BitData(Range.closed(1950L, 2150L), singletonList(2050L)));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), singletonList(3150L)));

        assertThat(pulsePairs, is(equalTo(expectedData)));
    }

    // Collection of 2 range pulses should come back as averages for symmetric pulses
    @Test
    public void shouldGetAverageSymmetricPulsesFromTwoRangePulseList() {
        List<BitData> pulsePairs = getZeroAndOnePulsePairs(DUAL_SYMBOL_RANGES, DUAL_SYMBOL_PULSE_LIST);

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(950L, 1150L), singletonList(1050L)));
        expectedData.add(new BitData(Range.closed(3050L, 3250L), singletonList(3150L)));

        assertThat(pulsePairs, is(equalTo(expectedData)));
    }

    // Collection of 2 range pulses should come back as distributed asymmetric pulses
    @Test
    public void shouldGetAverageAsymmetricPulsesFromTwoRangePulseList() {
        List<BitData> pulsePairs = getZeroAndOnePulsePairs(BCS_QUEST_RANGES, BCS_QUEST_PULSES);

        List<BitData> expectedData = new ArrayList<>();

        expectedData.add(new BitData(Range.closed(1500L, 1800L), asList(815L, 815L)));
        expectedData.add(new BitData(Range.closed(2200L, 2400L), asList(763L, 1526L)));

        assertThat(pulsePairs, is(equalTo(expectedData)));
    }
}