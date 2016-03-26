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
package xyz.meunier.wav2pzx.blocks;

import java.util.Collections;
import java.util.List;

/**
 * Null block that has no output representation. Used when we need to put a
 * placeholder block into a tape as we may need to revert it in the future.
 * @author Fredrick Meunier
 */
public final class PZXNullBlock implements PZXBlock {

	/* (non-Javadoc)
	 * @see xyz.meunier.wav2pzx.PZXBlock#getPZXBlockDiskRepresentation()
	 */
	@Override
	public byte[] getPZXBlockDiskRepresentation() {
		// No disk representation
		return new byte[0];
	}

	/* (non-Javadoc)
	 * @see xyz.meunier.wav2pzx.PZXBlock#getSummary()
	 */
	@Override
	public String getSummary() {
        return "Null PZX block";
	}

	/* (non-Javadoc)
	 * @see xyz.meunier.wav2pzx.PZXBlock#getPulses()
	 */
	@Override
	public List<Double> getPulses() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see xyz.meunier.wav2pzx.PZXBlock#getFirstPulseLevel()
	 */
	@Override
	public int getFirstPulseLevel() {
		return 0;
	}

}
