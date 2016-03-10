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
 * Some routines for working with pulses.
 * 
 * @author Fredrick Meunier
 */
public class PulseUtils {

	// The PZX specification suggests that pulses within 2% of each other should be considered equal
	private static final double ERROR_PERCENTAGE = 0.02;

	/**
	 * Compares two pulses sampled from a source and determine whether they are likely to be the same length.
	 * We can expect to resolve features of up to half the sample rate (based on Nyquist) and also need to allow
	 * for some error from the Spectrum ROM sample writing and the analogue tape of approximately 2% based on the
	 * PZX specification.
	 * @param pulse1 the first pulse to compare
	 * @param pulse2 the second pulse to compare
	 * @param resolution the feature resolution in terms of the target clock rate
	 * @return true if the two pulses are probably equal in duration
	 */
	public static boolean equalWithinResoution(double pulse1, double pulse2, double resolution) {
		double error = Double.max(pulse1, pulse2) * ERROR_PERCENTAGE;
		return Math.abs(pulse1 - pulse2) < (resolution + error);
	}
}