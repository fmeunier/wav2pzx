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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static xyz.meunier.wav2pzx.generaldecoder.LoaderState.*;

public class LoaderStateTest {

    private LoaderContext context;

    @Before
    public void setUp() throws Exception {
        context = mock(LoaderContext.class);
    }

    /**
     * Test of values method, of class LoaderState.
     */
    @Test
    public void testValues() {
        assertThat(LoaderState.values(), is(equalTo(new LoaderState[]{INITIAL, FIND_PILOT, FIND_PILOT_END, GET_DATA})));
    }

    /**
     * Test of valueOf method, of class LoaderState.
     */
    @Test
    public void testValueOf() {
        assertThat(INITIAL, is(LoaderState.valueOf("INITIAL")));
        assertThat(FIND_PILOT, is(LoaderState.valueOf("FIND_PILOT")));
        assertThat(FIND_PILOT_END, is(LoaderState.valueOf("FIND_PILOT_END")));
        assertThat(GET_DATA, is(LoaderState.valueOf("GET_DATA")));
    }

    @Test
    public void testNextState_From_INITIAL_To_FIND_PILOT() {
        when(context.isaPilotCandidate()).thenReturn(true);

        assertThat(INITIAL.nextState(context), is(FIND_PILOT));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).completeUnknownPulseBlock();
        inOrder.verify(context).addPilotPulse();
    }

    @Test
    public void testNextState_From_INITIAL_To_INITIAL() {
        when(context.isaPilotCandidate()).thenReturn(false);

        assertThat(INITIAL.nextState(context), is(INITIAL));

        verify(context).addUnclassifiedPulse();
        verify(context, never()).completeUnknownPulseBlock();
    }

    @Test
    public void testNextState_From_FIND_PILOT_To_INITIAL() {
        when(context.isaPilotCandidate()).thenReturn(false);

        assertThat(FIND_PILOT.nextState(context), is(INITIAL));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).revertCurrentBlock();
        inOrder.verify(context).addUnclassifiedPulse();
    }

    @Test
    public void testNextState_From_FIND_PILOT_To_FIND_PILOT_END() {
        when(context.isaMinimumNumberOfPilotPulses()).thenReturn(true);
        when(context.isaPilotCandidate()).thenReturn(true);

        assertThat(FIND_PILOT.nextState(context), is(FIND_PILOT_END));

        verify(context).addPilotPulse();
    }

    @Test
    public void testNextState_From_FIND_PILOT_To_FIND_PILOT() {
        when(context.getNumPilotPulses()).thenReturn(5);
        when(context.isaPilotCandidate()).thenReturn(true);

        assertThat(FIND_PILOT.nextState(context), is(FIND_PILOT));

        verify(context).addPilotPulse();
    }

    @Test
    public void testNextState_From_FIND_PILOT_END_To_GET_DATA() {
        when(context.isaDataCandidate()).thenReturn(true);

        assertThat(FIND_PILOT_END.nextState(context), is(GET_DATA));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).completePilotPulseBlock();
        inOrder.verify(context).addUnclassifiedPulse();
    }

    @Test
    public void testNextState_From_FIND_PILOT_END_To_INITIAL() {
        when(context.isaDataCandidate()).thenReturn(false);
        when(context.isTooLongToBeAPilot()).thenReturn(true);

        assertThat(FIND_PILOT_END.nextState(context), is(INITIAL));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).completePilotPulseBlock();
        inOrder.verify(context).addUnclassifiedPulse();
    }

    @Test
    public void testNextState_From_FIND_PILOT_END_To_FIND_PILOT_END() {
        when(context.isaDataCandidate()).thenReturn(false);
        when(context.isTooLongToBeAPilot()).thenReturn(false);

        assertThat(FIND_PILOT_END.nextState(context), is(FIND_PILOT_END));

        verify(context).addPilotPulse();
    }

    @Test
    public void testNextState_From_GET_DATA_To_GET_DATA() {
        when(context.isCurrentAndNextPulseTooLongToBeADataCandidate()).thenReturn(false);

        assertThat(GET_DATA.nextState(context), is(GET_DATA));

        verify(context).addUnclassifiedPulse();
    }

    @Test
    public void testNextState_From_GET_DATA_To_INITIAL_currentPulseTail() {
        // Current pulse is a good size to be a tail pulse but following is too long to be a data pulse
        when(context.isCurrentAndNextPulseTooLongToBeADataCandidate()).thenReturn(true);
        when(context.isaCandidateTailPulse()).thenReturn(true);

        assertThat(GET_DATA.nextState(context), is(INITIAL));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).addUnclassifiedPulse();
        inOrder.verify(context).completeDataBlock();
    }

    @Test
    public void testNextState_From_GET_DATA_To_INITIAL_currentPulseNotTail() {
        // Current pulse is a good size to be a tail pulse but following is too long to be a data pulse
        when(context.isCurrentAndNextPulseTooLongToBeADataCandidate()).thenReturn(true);
        when(context.isaCandidateTailPulse()).thenReturn(false);

        assertThat(GET_DATA.nextState(context), is(INITIAL));

        InOrder inOrder = inOrder(context);
        inOrder.verify(context).completeDataBlock();
        inOrder.verify(context).addUnclassifiedPulse();
    }

    @Test
    public void testEndLoader_INITIAL() {
        INITIAL.endLoader(context);
        verify(context).completeUnknownPulseBlock();
    }

    @Test
    public void testEndLoader_FIND_PILOT() {
        FIND_PILOT.endLoader(context);
        verify(context).completeUnknownPulseBlock();
    }

    @Test
    public void testEndLoader_FIND_PILOT_END() {
        FIND_PILOT_END.endLoader(context);
        verify(context).completePilotPulseBlock();
    }

    @Test
    public void testEndLoader_GET_DATA() {
        GET_DATA.endLoader(context);
        verify(context).completeDataBlock();
    }

}