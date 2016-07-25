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

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.singleton;
import static java.lang.Math.round;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.averagingLong;
import static java.util.stream.Collectors.toList;

/**
 * RangeFinder has utility methods to analyse a list of pulses and classify them into different classes corresponding
 * to the theoretical idealised underlying pulse generated by the saving computer.
 */
final class RangeFinder {

    // If the gap between the last pair sum and the next one is > 500 then it is likely a different symbol
    private static final int MIN_INTER_SYMBOL_GAP = 300;
    private static final int PULSE_TOLERANCE = 50;

    private RangeFinder() {
    }

    /**
     * Analyse a provided collection of summed pairs of pulses assuming that they consist of a typical ZX Spectrum data
     * block with zero and one bits represented by pairs of equal sized pulses and return a list of Ranges that can be
     * used to classify the source pulses into their different underlying groups.
     *
     * @param fullPulses the list of pulses to be analysed
     * @return the list of Ranges identified to correspond to the underlying pulse pairs
     * @throws NullPointerException if pulses was null
     */
    static List<Range<Long>> getRanges(Collection<Long> fullPulses) {
        checkNotNull(fullPulses, "fullPulses was null");

        List<Long> sortedPulses = fullPulses.stream().sorted().collect(toList());

        Optional<Long> first = sortedPulses.stream().findFirst();
        if (!first.isPresent()) {
            return emptyList();
        }

        Long bottomOfRange = first.get();
        if (sortedPulses.size() == 1) {
            return singletonList(singleton(bottomOfRange));
        }

        List<Range<Long>> foundRanges = new ArrayList<>();

        Long lastPulse = bottomOfRange;

        for (Long pulse : sortedPulses) {
            if (isaSignificantGapBetweenPulses(lastPulse, pulse)) {
                // We have a range
                foundRanges.add(getRange(bottomOfRange, lastPulse));

                bottomOfRange = pulse;
            }
            lastPulse = pulse;
        }

        if (bottomOfRange.equals(lastPulse)) {
            foundRanges.add(singleton(bottomOfRange));
        } else {
            foundRanges.add(getRange(bottomOfRange, lastPulse));
        }

        return ImmutableList.copyOf(foundRanges);
    }

    /**
     * Get an average pulse for pulses in a supplied stream that fall into the supplied ranges.
     *
     * @param ranges   the classified ranges
     * @param fullBits the pulses to classify
     * @return a map of range to the average length of the matching pulses from the stream
     * @throws NullPointerException if ranges or fullBits was null
     */
    static List<BitData> getReplacementBitDataOfRanges(Collection<Range<Long>> ranges, Collection<Long> fullBits) {
        checkNotNull(ranges, "ranges was null");
        checkNotNull(fullBits, "fullBits was null");

        List<BitData> averages = new ArrayList<>();

        for (Range<Long> range : ranges) {
            Long average = round(fullBits.stream().filter(range::contains).collect(averagingLong(Long::longValue)));
            averages.add(new BitData(range, singletonList(average)));
        }
        return averages;
    }

    static List<BitData> getZeroAndOnePulsePairs(Iterable<Range<Long>> ranges, Collection<Long> fullBits) {
        checkNotNull(ranges, "ranges was null");
        checkNotNull(fullBits, "fullBits was null");

        List<BitData> pulses = new ArrayList<>();

        for (Range<Long> range : ranges) {
            Long average = round(fullBits.stream().filter(range::contains).collect(averagingLong(Long::longValue)));
            addBit(pulses, range, average, 2);
        }

        // Based on the resolution of the data from tape it seems that it is only reliable to process pulses as
        // dual pulses. A quick review of the loaders on the preservation team site suggests that nearly all loaders
        // are dual pulse, with the 1 pulse as double the pulse length of the 0 pulse.
        // The notable exception of the software projects loader which has 780, 780 as 0 and 780, 1560 as 1. In this
        // case we have dual pulses where the dual pulse for 1 is 3/2 of the pulse length of the 0 one.

        // Analyse the pairs, summarise the pairs and make ranges that cover them (plus or minus 50 tstates)
        // Look at ratio of smaller to larger pulse total and distribute based on that. Standard loaders have 1 as
        // double 0 so 1/2, also possible to get 2/3, for 1/2 distribute both average total pulse pairs 50/50, for 2/3
        // distribute 0 50/50 and 1 33/66

        if (pulses.size() == 2) {
            List<BitData> newPulses = new ArrayList<>();

            BitData bit0 = pulses.get(0);
            Long averageBit0 = bit0.getFullPulse();
            BitData bit1 = pulses.get(1);
            Long averageBit1 = bit1.getFullPulse();

            // Assume bit0 is symmetrical and bit1 has a common divisor
            addBit(newPulses, bit0.getQualificationRange(), averageBit0, 2);

            long denominator = Math.round(averageBit1 / (averageBit0 / 2.0));

            addBit(newPulses, bit1.getQualificationRange(), averageBit1, denominator);

            pulses = newPulses;
        }

        // When we reprocess the block, do so pairwise and use 0 and 1 pairs to substitute the source data. Break blocks
        // where we don't have a match to either range.
        return pulses;
    }

    private static void addBit(List<BitData> pulses, Range<Long> range, Long value, long denominator) {
        long pulse0Numerator = denominator / 2;
        // If it's asymmetrical weight the second pulse higher
        long pulse1Numerator = (denominator / 2) + denominator % 2;

        Long pulse0 = value * pulse0Numerator / denominator;
        Long pulse1 = value * pulse1Numerator / denominator;

        pulses.add(new BitData(range, ImmutableList.of(pulse0, pulse1)));
    }

    /**
     * Constructs a list of ranges, one singleton range for each provided pulse
     *
     * @param pulses the collection of pulses
     * @return a list of singleton ranges, one for each non-null source pulse
     */
    static List<Range<Long>> getRangesForSinglePulses(Collection<Long> pulses) {
        checkNotNull(pulses, "pulses was null");
        return pulses.stream().filter(Objects::nonNull).sorted().map(Range::singleton).collect(toList());
    }

    /**
     * Get a map of supplied Range<Long> singleton values to their long value.
     *
     * @param ranges the classified ranges
     * @return a map of range to the length of the matching pulses from the stream
     * @throws NullPointerException if ranges was null
     */
    static List<BitData> getSingletonPulseLengthsOfRanges(Iterable<Range<Long>> ranges) {
        checkNotNull(ranges, "ranges was null");

        LinkedHashSet<BitData> averages = new LinkedHashSet<>();

        for(Range<Long> range : ranges) {
            averages.add(new BitData(range, singletonList(range.lowerEndpoint())));
        }
        return averages.stream().collect(toList());
    }

    private static boolean isaSignificantGapBetweenPulses(Long lastPulse, Long pulse) {
        return (pulse - lastPulse) > MIN_INTER_SYMBOL_GAP;
    }

    private static Range<Long> getRange(Long bottomOfRange, Long lastPulse) {
        return closed(bottomOfRange - PULSE_TOLERANCE, lastPulse + PULSE_TOLERANCE);
    }

}
