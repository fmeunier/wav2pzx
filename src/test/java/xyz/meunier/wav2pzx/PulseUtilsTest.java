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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Fredrick Meunier
 */
public class PulseUtilsTest {

	/**
	 * Test method for {@link xyz.meunier.wav2pzx.PulseUtils#equalWithinResoution(double, double, double)}.
	 */
	@Test
	public final void testEqualWithinResoution() {
		assertTrue("Small average sample pilot pulse from a real tape", PulseUtils.equalWithinResoution(LoaderContext.PILOT_LENGTH, 2058, 76));
		assertFalse("Upper bound of non-matching pulse", PulseUtils.equalWithinResoution(LoaderContext.PILOT_LENGTH, 1983, 76));
		assertTrue("Upper bound of a matching pilot pulse", PulseUtils.equalWithinResoution(LoaderContext.PILOT_LENGTH, 2362, 76));
		assertFalse("Lower bound of non-matching pulse", PulseUtils.equalWithinResoution(LoaderContext.PILOT_LENGTH, 2363, 76));
	}

}
