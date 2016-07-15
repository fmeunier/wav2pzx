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
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TapeBlock is an immutable class representing a block identified on the tape. It has a type, a mapping of ranges of
 * pulse lengths to idealised pulse lengths and the pulses in the source block.
 */
final class TapeBlock {

    private final BlockType blockType;
    private final ImmutableList<BitData> bitDataList;
    private final PulseList pulseList;

    TapeBlock(BlockType blockType, List<BitData> bitDataList, PulseList pulseList) {
        checkNotNull(blockType, "blockType cannot be null");
        checkNotNull(bitDataList, "ranges cannot be null");
        checkNotNull(pulseList, "pulseList cannot be null");

        this.blockType = blockType;
        this.bitDataList = ImmutableList.copyOf(bitDataList);
        this.pulseList = pulseList;
    }

    /**
     * Returns the block type
     * @return the type of the block
     */
    BlockType getBlockType() {
        return this.blockType;
    }

    /**
     * Returns a map of pulse length ranges to an idealised pulse length
     * @return the pulse range to idealised pulse length map
     */
    ImmutableList<BitData> getBitDataList() {
        return this.bitDataList;
    }

    /**
     * Returns the list of pulses in the identified block
     * @return the PulseList from this block
     */
    public PulseList getPulseList() {
        return this.pulseList;
    }

    @Override
    public String toString() {
        return "TapeBlock{" +
                "blockType=" + blockType +
                ", bitDataList=" + bitDataList +
                ", pulseList=" + pulseList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TapeBlock tapeBlock = (TapeBlock) o;

        if (blockType != tapeBlock.blockType) return false;
        if (bitDataList != null ? !bitDataList.equals(tapeBlock.bitDataList) : tapeBlock.bitDataList != null)
            return false;
        return pulseList != null ? pulseList.equals(tapeBlock.pulseList) : tapeBlock.pulseList == null;

    }

    @Override
    public int hashCode() {
        int result = blockType != null ? blockType.hashCode() : 0;
        result = 31 * result + (bitDataList != null ? bitDataList.hashCode() : 0);
        result = 31 * result + (pulseList != null ? pulseList.hashCode() : 0);
        return result;
    }
}
