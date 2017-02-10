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

import xyz.meunier.wav2pzx.pulselist.PulseList;
import xyz.meunier.wav2pzx.pulselist.PulseListBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.round;

/**
 * This class converts a stream of 0/1 samples into a PulseList
 *
 * @author Fredrick Meunier
 */
public final class SamplePulseGenerator {

    private final PulseListBuilder builder;
    private int lastSampleLevel;
    private boolean gotFirstSample;
    private double currentPulseDuration;
    private boolean tapeComplete;
    private PulseList pulseList;

    public SamplePulseGenerator() {
        builder = new PulseListBuilder();
        gotFirstSample = false;
        tapeComplete = false;
    }

    /**
     * @return whether this builder has completed and built the tape
     */
    boolean isTapeComplete() {
        return tapeComplete;
    }

    /**
     * Add a sample from the source to the PulseList under construction
     *
     * @param newLevel new level, range is expected to be 0 - 1
     * @throws IllegalStateException    if the tape is complete
     * @throws IllegalArgumentException if the sample is out of range
     */
    void addSample(int newLevel, double tStatesSinceLastSample) {
        // State error, tape is already complete so no more pulses
        checkState(!tapeComplete, "Pulse length list has already been marked as complete");
        checkArgument(newLevel >= 0 & newLevel <= 1, "Level out of range, should be 0-1, value: " + newLevel);

        // Record the level of the first sample, later pulses only record the duration
        // as they are defined by the edge between the levels
        if (!gotFirstSample) {
            gotFirstSample = true;
            builder.withFirstPulseLevel(newLevel);
            lastSampleLevel = newLevel;
            currentPulseDuration = tStatesSinceLastSample;
            return;
        }

        if (newLevel == lastSampleLevel) {
            // Continue current pulse
            currentPulseDuration += tStatesSinceLastSample;
        } else {
            // Close current pulse and start accumulating new pulse
            builder.withNextPulse(round(currentPulseDuration));
            currentPulseDuration = tStatesSinceLastSample;
            lastSampleLevel = newLevel;
        }
    }

    /**
     * Construct the new PulseList and mark the tape as being complete
     *
     * @return the PulseList
     * @throws IllegalStateException if we haven't yet processed any samples from the tape
     */
    public PulseList build() {
        // State error, haven't received first pulse so we don't know the first pulse level
        checkState(gotFirstSample, "First pulse not yet received");

        if (tapeComplete) {
            return pulseList;
        }

        // Close current pulse and mark list as being complete
        builder.withNextPulse(round(currentPulseDuration));
        tapeComplete = true;
        pulseList = builder.build();

        return pulseList;
    }
}
