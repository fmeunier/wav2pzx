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

/**
 * Mock object for the LoaderContext interface to use in unit tests
 * @author Fredrick Meunier
 */
public final class MockLoaderContext implements LoaderContext {

    private int bit;
    private Double lastSecondOnePulse;
    private Double lastPilotPulse;
    private Double lastSync1Length;
    private Double lastSync2Length;
    private Double lastUnclassifiedPulse;
    private Double lastSecondZeroPulse;
    private boolean calledCompleteDataBlock;
    private boolean lastIsPilot;
    private boolean calledCompletePulseBlock;
    private int lastNumPilotPulses;
    private double lastTailLength;
    private boolean calledResetBlock;
    private boolean calledRevertCurrentBlock;
    private double currentPulse;
    private int currentPulseLevel;
    private Double nextPulseLevel;
    private boolean hasNextPulse;
    private Double lastFirstZeroPulse;
    private Double lastFirstOnePulse;

    public MockLoaderContext() {
        resetFields();
    }

    public void resetFields() {
        this.bit = 0;
        this.lastFirstOnePulse = Double.NaN;
        this.lastSecondOnePulse = Double.NaN;
        this.lastPilotPulse = Double.NaN;
        this.lastSync1Length = Double.NaN;
        this.lastSync2Length = Double.NaN;
        this.lastUnclassifiedPulse = Double.NaN;
        this.lastFirstZeroPulse = Double.NaN;
        this.lastSecondZeroPulse = Double.NaN;
        this.calledCompleteDataBlock = false;
        this.lastIsPilot = false;
        this.calledCompletePulseBlock = false;
        this.lastNumPilotPulses = Integer.MIN_VALUE;
        this.lastTailLength = Double.NaN;
        this.calledResetBlock = false;
        this.calledRevertCurrentBlock = false;
        this.currentPulse = Double.NaN;
        this.currentPulseLevel = Integer.MIN_VALUE;
        this.nextPulseLevel = Double.NaN;
        this.hasNextPulse = false;
    }

    public int getBit() {
        return bit;
    }

    public Double getLastFirstOnePulse() {
        return lastFirstOnePulse;
    }

    public Double getLastSecondOnePulse() {
        return lastSecondOnePulse;
    }

    public Double getLastPilotPulse() {
        return lastPilotPulse;
    }

    public Double getLastUnclassifiedPulse() {
        return lastUnclassifiedPulse;
    }

    public Double getLastFirstZeroPulse() {
        return lastFirstZeroPulse;
    }

    public Double getLastSecondZeroPulse() {
        return lastSecondZeroPulse;
    }

    public boolean isCalledCompleteDataBlock() {
        return calledCompleteDataBlock;
    }

    public boolean isCalledCompletePulseBlock() {
        return calledCompletePulseBlock;
    }

    public boolean isCalledResetBlock() {
        return calledResetBlock;
    }

    public boolean isCalledRevertCurrentBlock() {
        return calledRevertCurrentBlock;
    }

    @Override
    public void addBit(int bit) {
        this.bit = bit;
    }

    /**
     *
     * @param firstPulseLength the value of firstPulseLength
     * @param secondPulseLength the value of secondPulseLength
     */
    @Override
    public void addOnePulse(Double firstPulseLength, Double secondPulseLength) {
        this.lastFirstOnePulse = firstPulseLength;
        this.lastSecondOnePulse = secondPulseLength;

    }

    @Override
    public void addPilotPulse(Double pulseLength) {
        this.lastPilotPulse = pulseLength;
    }

    @Override
    public void addSync1(Double pulseLength) {
        this.lastSync1Length = pulseLength;
    }

    @Override
    public void addSync2(Double pulseLength) {
        this.lastSync2Length = pulseLength;
    }

    @Override
    public void addUnclassifiedPulse(Double pulseLength) {
        this.lastUnclassifiedPulse = pulseLength;
    }

    /**
     *
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    @Override
    public void addZeroPulse(Double firstPulseLength, Double secondPulseLength) {
        this.lastFirstZeroPulse = firstPulseLength;
        this.lastSecondZeroPulse = secondPulseLength;
    }

    @Override
    public void completeDataBlock() {
        this.calledCompleteDataBlock = true;
    }

    @Override
    public void completePulseBlock(boolean isPilot) {
        this.lastIsPilot = isPilot;
        this.calledCompletePulseBlock = true;
    }

    @Override
    public int getNumPilotPulses() {
        return this.lastNumPilotPulses;
    }

    @Override
    public double getSync1Length() {
        return this.lastSync1Length;
    }

    @Override
    public double getSync2Length() {
        return this.lastSync2Length;
    }

    @Override
    public double getTailLength() {
        return this.lastTailLength;
    }

    @Override
    public void resetBlock() {
        this.calledResetBlock = true;
    }

    @Override
    public void revertCurrentBlock() {
        this.calledRevertCurrentBlock = true;
    }

    @Override
    public void setTailLength(Double pulseLength) {
        this.lastTailLength = pulseLength;
    }
    
    public boolean isLastIsPilot() {
        return lastIsPilot;
    }

    void setNumPilotPulses(int numPulses) {
        this.lastNumPilotPulses = numPulses;
    }

    @Override
    public double getCurrentPulse() {
        return this.currentPulse;
    }

    public void setCurrentPulse(double currentPulse) {
        this.currentPulse = currentPulse;
    }
    
    @Override
    public int getCurrentPulseLevel() {
        return this.currentPulseLevel;
    }

    public void setCurrentPulseLevel(int currentPulseLevel) {
        this.currentPulseLevel = currentPulseLevel;
    }
    
    @Override
    public Double peekNextPulse() {
        return this.nextPulseLevel;
    }

    public void setNextPulse(Double nextPulseLevel) {
        this.nextPulseLevel = nextPulseLevel;
    }
    
    @Override
    public boolean hasNextPulse() {
        return this.hasNextPulse;
    }

    public void setHasNextPulse(boolean hasNextPulse) {
        this.hasNextPulse = hasNextPulse;
    }
    
    @Override
    public double getNextPulse() {
        return this.nextPulseLevel;
    }
}
