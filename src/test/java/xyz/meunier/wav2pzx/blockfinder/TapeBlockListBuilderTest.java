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

package xyz.meunier.wav2pzx.blockfinder;

import org.junit.Test;
import xyz.meunier.wav2pzx.PulseList;

import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TapeBlockListBuilderTest {

    private final TapeBlockListBuilder builder = new TapeBlockListBuilder();
    private final TapeBlock block = new TapeBlock(BlockType.UNKNOWN, Collections.emptyMap(), new PulseList(singletonList(200L), 0, 1));
    private final TapeBlock block2 = new TapeBlock(BlockType.UNKNOWN, Collections.emptyMap(), new PulseList(singletonList(400L), 0, 1));

    @Test
    public void shouldGetAnEmptyListWhenNoBlocksSupplied() {
        assertThat(builder.build(), is(emptyList()));
    }

    @Test
    public void shouldGetAnEmptyOptionalWhenPeekingAtAnEmptyBuilder() {
        assertThat(builder.peekLastBlock(), is(empty()));
    }

    @Test
    public void shouldHaveBlockOnListWhenAdded() {
        builder.add(block);
        assertThat(builder.build(), is(singletonList(block)));
    }

    @Test
    public void shouldGetLastAddedBlockWhenPeeking() {
        builder.add(block);
        assertThat(builder.peekLastBlock(), is(Optional.of(block)));
        assertThat(builder.build(), is(singletonList(block)));
    }

    @Test
    public void shouldGetAndRemoveLastAddedBlockWhenRemoving() {
        builder.add(block);
        assertThat(builder.removeLastBlock(), is(Optional.of(block)));
        assertThat(builder.build(), is(emptyList()));
    }

    @Test
    public void shouldGetAllNonNullBlocksInAddedList() {
        builder.addAll(asList(Optional.of(block), null, Optional.of(block2)));
        assertThat(builder.build(), is(asList(block, block2)));
    }
}