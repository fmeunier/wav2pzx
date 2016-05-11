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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Helper class for building a list of data bytes one bit at a time from a serial source.
 */
public final class DataBuilder {

    private boolean blockComplete = false;
    private List<Byte> data = new ArrayList<>();

    private byte currentByte = 0;
    private int numBitsInCurrentByte = 0;

    /**
     * Return an immutable copy of the accumulated data and mark the block complete
     * @return the immutable copy of the accumulated data
     */
    public ImmutableList<Byte> getData() {
        if(!blockComplete) {
            completeData();
        }
        return ImmutableList.copyOf(data);
    }

    /**
     * Returns the number of bits in the current byte being accumulated
     * @return the number of bits in the current byte being accumulated
     */
    public int getNumBitsInCurrentByte() {
        return numBitsInCurrentByte == 0 && !data.isEmpty() ? 8 : numBitsInCurrentByte;
    }

    /**
     * Adds the supplied bit to the data being accumulated
     * @param bit the new bit to add to the data block
     */
    public void addBit(int bit) {
        checkState(!blockComplete);
        currentByte <<= 1;
        currentByte |= (bit & 0x01);
        if( ++numBitsInCurrentByte == 8 ) {
            data.add(currentByte);
            currentByte = 0;
            numBitsInCurrentByte = 0;
        }
    }

    public byte getCurrentByte() {
        return currentByte;
    }

    private void completeData() {
        // add any partially accumulated byte to the data collection before
        // considering this block complete
        if( numBitsInCurrentByte != 0 ) {
            data.add(currentByte);
        }
        blockComplete = true;
    }
}
