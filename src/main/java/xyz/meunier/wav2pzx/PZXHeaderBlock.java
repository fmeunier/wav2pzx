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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import static xyz.meunier.wav2pzx.PZXEncodeUtils.addPZXBlockHeader;

/**
 * Represents the header block (PZXT) at the beginning of all valid PZX files.
 * <p>
 * Currently hardcoded for PZX 1.0
 * @author Fredrick Meunier
 */
public class PZXHeaderBlock implements PZXBlock {
    /*
     * PZXT - PZX header block
     * -----------------------
     * 
     * offset type     name   meaning
     * 0      u8       major  major version number (currently 1).
     * 1      u8       minor  minor version number (currently 0).
     * 2      u8[?]    info   tape info, see below.
     * 
     * This block distinguishes the PZX files from other files and may provide
     * additional info about the file as well. This block must be always present as
     * the first block of any PZX file.
     */
    final static byte PZX_MAJOR_VERSION = 0x01;
    final static byte PZX_MINOR_VERSION = 0x00;
    
    @Override
    public byte[] getPZXBlockDiskRepresentation() {
        /*
         * offset type     name   meaning
         * 8      u8       major  major version number (currently 1).
         * 9      u8       minor  minor version number (currently 0).
         */
        final byte[] pzxHeaderBlockData = ByteBuffer.allocate(2)
                .put(PZX_MAJOR_VERSION).put(PZX_MINOR_VERSION).array();
        return addPZXBlockHeader("PZXT", pzxHeaderBlockData);
    }

    @Override
    public String getSummary() {
        return "PZX major version: " + PZX_MAJOR_VERSION + " minor version: " + PZX_MINOR_VERSION;
    }

    @Override
    public List<Double> getPulses() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "PZXHeaderBlock{" + getSummary() + '}';
    }

	@Override
	public int getFirstPulseLevel() {
		return 0;
	}

}
