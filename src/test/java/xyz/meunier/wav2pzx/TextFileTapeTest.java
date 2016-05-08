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

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class TextFileTapeTest {

    @Test(expected = NullPointerException.class)
    public void getPulsesShouldNotAcceptNullList() throws Exception {
        TextFileTape.getPulses(null);
    }

    @Test
    public void getPulsesShouldIgnoreEmptyLines() throws Exception {
        assertThat(TextFileTape.getPulses(asList("", "0 : 1").stream()), contains(0L));
    }

    @Test
    public void getPulsesShouldProcessAListCorrectly() throws Exception {
        assertThat(TextFileTape.getPulses(asList("10 : 0", "0 : 1", "50 : 0").stream()), contains(10L, 0L, 50L));
    }

    @Test
    public void getPulsesShouldDiscardNegativePulses() throws Exception {
        assertThat(TextFileTape.getPulses(asList("10 : 0", "0 : 1", "-50 : 0").stream()), contains(10L, 0L));
    }

    @Test
    public void getPulsesShouldDiscardInvalidNumberPulses() throws Exception {
        assertThat(TextFileTape.getPulses(asList("10 : 0", "0 : 1", "q : 0").stream()), contains(10L, 0L));
    }

    @Test(expected = NullPointerException.class)
    public void getInitialLevelShouldNotAcceptNullList() throws Exception {
        TextFileTape.getInitialLevel(null);
    }

    @Test
    public void getInitialLevelShouldDefaultToZero() throws Exception {
        List<String> strings = emptyList();
        assertThat(TextFileTape.getInitialLevel(strings.stream()), is(0));
    }

    @Test
    public void getInitialLevelShouldGetFirstNonEmptyStringInFile() throws Exception {
        assertThat(TextFileTape.getInitialLevel(asList("", "0 : 1").stream()), is(1));
    }

    @Test
    public void getInitialLevelShouldDefaultToZeroForInvalidNumbers() throws Exception {
        assertThat(TextFileTape.getInitialLevel(singletonList("0 : q").stream()), is(0));
    }

    @Test
    public void getInitialLevelShouldProcessAllNonZeroPositiveNumbersAsOne() throws Exception {
        assertThat(TextFileTape.getInitialLevel(singletonList("0 : 10").stream()), is(1));
    }

    @Test
    public void getInitialLevelShouldProcessAllNonZeroNegativeNumbersAsZero() throws Exception {
        assertThat(TextFileTape.getInitialLevel(singletonList("0 : -10").stream()), is(0));
    }
}