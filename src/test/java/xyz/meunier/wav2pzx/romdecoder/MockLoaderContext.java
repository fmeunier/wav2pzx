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
package xyz.meunier.wav2pzx.romdecoder;

/**
 * Mock object for the LoaderContext interface to use in unit tests
 * @author Fredrick Meunier
 */
public final class MockLoaderContext implements LoaderContext {

    private Long lastSecondOnePulse;
    private Long lastPilotPulse;
    private Long lastSync1Length;
    private Long lastSync2Length;
    private Long lastUnclassifiedPulse;
    private Long lastSecondZeroPulse;
    private boolean calledCompleteDataBlock;
    private boolean lastIsPilot;
    private boolean calledCompletePulseBlock;
    private int lastNumPilotPulses;
    private Long lastTailLength;
    private boolean calledResetBlock;
    private boolean calledRevertCurrentBlock;
    private Long currentPulse;
    private int currentPulseLevel;
    private Long nextPulseLevel;
    private boolean hasNextPulse;
    private Long lastFirstZeroPulse;
    private Long lastFirstOnePulse;

    public MockLoaderContext() {
        resetFields();
    }

    public void resetFields() {
        this.lastFirstOnePulse = Long.MIN_VALUE;
        this.lastSecondOnePulse = Long.MIN_VALUE;
        this.lastPilotPulse = Long.MIN_VALUE;
        this.lastSync1Length = Long.MIN_VALUE;
        this.lastSync2Length = Long.MIN_VALUE;
        this.lastUnclassifiedPulse = Long.MIN_VALUE;
        this.lastFirstZeroPulse = Long.MIN_VALUE;
        this.lastSecondZeroPulse = Long.MIN_VALUE;
        this.calledCompleteDataBlock = false;
        this.lastIsPilot = false;
        this.calledCompletePulseBlock = false;
        this.lastNumPilotPulses = Integer.MIN_VALUE;
        this.lastTailLength = Long.MIN_VALUE;
        this.calledResetBlock = false;
        this.calledRevertCurrentBlock = false;
        this.currentPulse = Long.MIN_VALUE;
        this.currentPulseLevel = Integer.MIN_VALUE;
        this.nextPulseLevel = Long.MIN_VALUE;
        this.hasNextPulse = false;
    }

    public Long getLastFirstOnePulse() {
        return lastFirstOnePulse;
    }

    public Long getLastSecondOnePulse() {
        return lastSecondOnePulse;
    }

    public Long getLastPilotPulse() {
        return lastPilotPulse;
    }

    public Long getLastUnclassifiedPulse() {
        return lastUnclassifiedPulse;
    }

    public Long getLastFirstZeroPulse() {
        return lastFirstZeroPulse;
    }

    public Long getLastSecondZeroPulse() {
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

    /**
     *
     * @param firstPulseLength the value of firstPulseLength
     * @param secondPulseLength the value of secondPulseLength
     */
    @Override
    public void addOnePulse(Long firstPulseLength, Long secondPulseLength) {
        this.lastFirstOnePulse = firstPulseLength;
        this.lastSecondOnePulse = secondPulseLength;

    }

    @Override
    public void addPilotPulse(Long pulseLength) {
        this.lastPilotPulse = pulseLength;
    }

    @Override
    public void addSync1(Long pulseLength) {
        this.lastSync1Length = pulseLength;
    }

    @Override
    public void addSync2(Long pulseLength) {
        this.lastSync2Length = pulseLength;
    }

    @Override
    public void addUnclassifiedPulse(Long pulseLength) {
        this.lastUnclassifiedPulse = pulseLength;
    }

    /**
     *
     * @param firstPulseLength the value of firstPulse
     * @param secondPulseLength the value of secondPulseLength
     */
    @Override
    public void addZeroPulse(Long firstPulseLength, Long secondPulseLength) {
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
    public long getSync1Length() {
        return this.lastSync1Length;
    }

    @Override
    public long getSync2Length() {
        return this.lastSync2Length;
    }

    @Override
    public long getTailLength() {
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
    public void setTailLength(Long pulseLength) {
        this.lastTailLength = pulseLength;
    }
    
    public boolean isLastIsPilot() {
        return lastIsPilot;
    }

    void setNumPilotPulses(int numPulses) {
        this.lastNumPilotPulses = numPulses;
    }

    @Override
    public long getCurrentPulse() {
        return this.currentPulse;
    }

    public void setCurrentPulse(long currentPulse) {
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
    public Long peekNextPulse() {
        return this.nextPulseLevel;
    }

    public void setNextPulse(Long nextPulseLevel) {
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
    public long getNextPulse() {
        return this.nextPulseLevel;
    }

	@Override
	public long getResolution() {
		return 1;
	}
}
