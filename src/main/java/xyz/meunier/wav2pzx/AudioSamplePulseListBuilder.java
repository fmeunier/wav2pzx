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
package xyz.meunier.wav2pzx;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.round;

/**
 * The aim of this class is to take a series of samples from an external source
 * and convert them into an array list of pulse durations in T states
 *
 * @author Fredrick Meunier
 */
public final class AudioSamplePulseListBuilder {

    private final PulseListBuilder builder;
    private int lastSampleLevel;
    private boolean gotFirstSample;
    private double currentPulseDuration;
    private final double tStatesPerSample;
    private boolean tapeComplete;
    private PulseList pulseList;
    private final Bistable bistable;

    /**
     * Construct a new AudioSamplePulseListBuilder.
     * @param sampleRate the sample rate of the source file, must be less than targetHz
     * @param targetHz the sample rate to resample to
     * @param trigger determines when the signal level of a sample should be 0 or 1
     */
    public AudioSamplePulseListBuilder(float sampleRate, float targetHz, Bistable trigger) {
        // Assert sampleRate > 0
        checkArgument(sampleRate > 0, "Sample rate must be greater than 0, sample rate: " + sampleRate);
        
        // Assert targetHz >= sampleRate
        // note that we expect targetHz to be in MHz and audio samples are not 
        // expected to be in this range for the foreseeable future
        checkArgument(targetHz >= sampleRate, "Target Hz must be greater than or equal to sample rate, target Hz:" + targetHz + " sample rate: " + sampleRate);

        tStatesPerSample = targetHz / sampleRate;
        builder = new PulseListBuilder().withResolution((int)round(tStatesPerSample));
        gotFirstSample = false;
        tapeComplete = false;
        bistable = trigger;
    }

    /**
     * @return the number of tstates per sample
     */
    public double getTStatesPerSample() {
        return tStatesPerSample;
    }

    /**
     * @return whether this builder has completed and built the tape
     */
    public boolean isTapeComplete() {
        return tapeComplete;
    }

    /**
     * Add a sample from the source to the PulseList under construction
     * @param sample new unsigned byte sample value, range is expected to be 0 - 255
     * @throws IllegalStateException if the tape is complete
     * @throws IllegalArgumentException if the sample is out of range
     */
    public void addSample(int sample) {
        // State error, tape is already complete so no more pulses
        checkState(!tapeComplete, "Pulse length list has already been marked as complete");
        checkArgument( sample >= 0 & sample <= 255, "Sample out of range, should be 0-255, value: " + sample);
        
        int newLevel = bistable.getNewLevel(sample);
        
        // Record the level of the first sample, later pulses only record the duration
        // as they are defined by the edge between the levels
        if( !gotFirstSample ) {
            gotFirstSample = true;
            builder.withFirstPulseLevel(newLevel);
            lastSampleLevel = newLevel;
            currentPulseDuration = tStatesPerSample;
            return;
        }
        
        if (newLevel == lastSampleLevel) {
            // Continue current pulse
            currentPulseDuration += tStatesPerSample;
        } else {
            // Close current pulse and start accumulating new pulse
            builder.withNextPulse(round(currentPulseDuration));
            currentPulseDuration = tStatesPerSample;
            lastSampleLevel = newLevel;
        }
    }

    /**
     * Construct the new PulseList and mark the tape as being complete
     * @return the PulseList
     * @throws IllegalStateException if we haven't yet processed any samples from the tape
     */
    public PulseList build() {
        // State error, haven't received first pulse so we don't know the first pulse level
        checkState(gotFirstSample, "First pulse not yet received");
                
        if(tapeComplete) {
            return pulseList;
        }
        
        // Close current pulse and mark list as being complete
        builder.withNextPulse(round(currentPulseDuration));
        tapeComplete = true;
        pulseList = builder.build();
        
        return pulseList;
    }
}
