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

import org.junit.Test;

import static org.junit.Assert.*;
import static xyz.meunier.wav2pzx.romdecoder.LoaderState.*;

/**
 *
 * @author Fredrick Meunier
 */
public class LoaderStateTest {

    public LoaderStateTest() {
    }

    /**
     * Test of values method, of class LoaderState.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        LoaderState[] expResult = {INITIAL, FIND_PILOT, FIND_SYNC1, GET_SYNC2, GET_DATA};
        LoaderState[] result = LoaderState.values();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class LoaderState.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "INITIAL";
        LoaderState expResult = INITIAL;
        LoaderState result = LoaderState.valueOf(name);
        assertEquals(expResult, result);
        name = "FIND_PILOT";
        expResult = FIND_PILOT;
        result = LoaderState.valueOf(name);
        assertEquals(expResult, result);
        name = "FIND_SYNC1";
        expResult = FIND_SYNC1;
        result = LoaderState.valueOf(name);
        assertEquals(expResult, result);
        name = "GET_SYNC2";
        expResult = GET_SYNC2;
        result = LoaderState.valueOf(name);
        assertEquals(expResult, result);
        name = "GET_DATA";
        expResult = GET_DATA;
        result = LoaderState.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of nextState method, of class LoaderState.
     */
    @Test
    public void testNextState() {
        // Test all transitions
        System.out.println("nextState");
        MockLoaderContext context = new MockLoaderContext();

        //logTransition(pulseLength, INITIAL, FIND_PILOT);
        context.setCurrentPulse(2100);
        LoaderState instance = LoaderState.INITIAL;
        LoaderState expResult = LoaderState.FIND_PILOT;
        LoaderState result = instance.nextState(context);
        assertEquals(expResult, result);
        assertTrue(context.isCalledCompletePulseBlock());
        assertFalse(context.isLastIsPilot());
        assertEquals(context.getCurrentPulse(), context.getLastPilotPulse().longValue());

        //logTransition(pulseLength, INITIAL, INITIAL);
        context.resetFields();
        context.setCurrentPulse(3500);
        instance = LoaderState.INITIAL;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertFalse(context.isCalledCompletePulseBlock());
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        
        //logTransition(pulseLength, FIND_PILOT, INITIAL);
        context.resetFields();
        context.setCurrentPulse(3500);
        instance = LoaderState.FIND_PILOT;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertTrue(context.isCalledRevertCurrentBlock());
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        
        //logTransition(pulseLength, FIND_PILOT, FIND_SYNC1);
        context.resetFields();
        context.setNumPilotPulses(LoaderContext.MIN_PILOT_COUNT);
        context.setCurrentPulse(2100);
        instance = LoaderState.FIND_PILOT;
        expResult = LoaderState.FIND_SYNC1;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertFalse(context.isCalledRevertCurrentBlock());
        assertEquals(context.getCurrentPulse(), context.getLastPilotPulse().longValue());
        
        //logTransition(pulseLength, FIND_PILOT, FIND_PILOT);
        context.resetFields();
        context.setNumPilotPulses(1);
        context.setCurrentPulse(2100L);
        instance = LoaderState.FIND_PILOT;
        expResult = LoaderState.FIND_PILOT;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastPilotPulse().longValue());        
        
        //logTransition(pulseLength, FIND_SYNC1, INITIAL);
        context.resetFields();
        context.setNumPilotPulses(1);
        context.setCurrentPulse(LoaderContextImpl.SYNC_TOTAL_MAX + 1);
        instance = LoaderState.FIND_SYNC1;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertTrue("Block was reverted", context.isCalledRevertCurrentBlock());
        assertFalse(context.isLastIsPilot());
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        assertNotEquals(context.getCurrentPulse(), context.getLastPilotPulse().longValue());        
        
        //logTransition(pulseLength, FIND_SYNC1, GET_SYNC2);
        context.resetFields();
        context.setCurrentPulse(LoaderContextImpl.SYNC1_MAX);
        instance = LoaderState.FIND_SYNC1;
        expResult = LoaderState.GET_SYNC2;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertFalse(context.isCalledCompletePulseBlock());
        assertEquals(context.getCurrentPulse(), context.getSync1Length());
        
        //logTransition(pulseLength, FIND_SYNC1, FIND_SYNC1);
        context.resetFields();
        context.setCurrentPulse(2500);
        instance = LoaderState.FIND_SYNC1;
        expResult = LoaderState.FIND_SYNC1;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertFalse(context.isCalledCompletePulseBlock());
        assertEquals(context.getCurrentPulse(), context.getLastPilotPulse().longValue());

        //logTransition(pulseLength, GET_SYNC2, GET_DATA);
        context.resetFields();
        context.addSync1((long)LoaderContext.SYNC1);
        context.setCurrentPulse((long)LoaderContext.SYNC2);
        instance = LoaderState.GET_SYNC2;
        expResult = LoaderState.GET_DATA;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getSync2Length());

        //logTransition(pulseLength, GET_SYNC2, INITIAL);
        context.resetFields();
        context.addSync1((long)LoaderContext.SYNC1);
        context.setCurrentPulse((long)LoaderContext.SYNC_TOTAL_MAX);
        instance = LoaderState.GET_SYNC2;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertTrue("Failure to get SYNC2 results in block being reverted", context.isCalledRevertCurrentBlock());
        assertFalse("Block is expected to be a plain pulse block, not a pilot block", context.isLastIsPilot());
        assertNotEquals(context.getCurrentPulse(), context.getSync2Length());
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());

        //logTransition(pulseLength, GET_DATA, GET_DATA);
        // Seven cases:
        // 1: the current pulse was not a tail pulse and there is no next pulse
        context.resetFields();
        context.setCurrentPulse(LoaderContext.PILOT_MAX + 1);
        context.setHasNextPulse(false);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        assertTrue(context.isCalledCompleteDataBlock());

        // 2: the current pulse was not a tail pulse and there is no next pulse
        context.resetFields();
        context.setCurrentPulse(LoaderContext.PILOT_MAX + 1);
        context.setHasNextPulse(false);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        assertTrue(context.isCalledCompleteDataBlock());

        // 3: current pulse is a zero pulse and next pulse is also a zero pulse so we have a zero bit
        context.resetFields();
        context.setNextPulse((long)LoaderContext.ZERO);
        context.setHasNextPulse(true);
        context.setCurrentPulse((long)LoaderContext.ZERO);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.GET_DATA;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastFirstZeroPulse().longValue());
        assertEquals(context.getNextPulse(), context.getLastSecondZeroPulse().longValue());

        // 4: current pulse is a one pulse and next pulse is also a one pulse so we have a one bit
        context.resetFields();
        context.setNextPulse((long)LoaderContext.ONE);
        context.setHasNextPulse(true);
        context.setCurrentPulse((long)LoaderContext.ONE);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.GET_DATA;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastFirstOnePulse().longValue());
        assertEquals(context.getNextPulse(), context.getLastSecondOnePulse().longValue());
        
        // 5: the current pulse was not a tail pulse and the next was a pilot pulse
        context.resetFields();
        context.setCurrentPulse(LoaderContext.PILOT_MAX + 1);
        context.setHasNextPulse(true);
        context.setNextPulse(2500L);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        assertTrue(context.isCalledCompleteDataBlock());

        // 6: the current pulse was not a tail pulse and the next was not a pilot pulse
        context.resetFields();
        context.setCurrentPulse(LoaderContext.PILOT_MAX + 1);
        context.setHasNextPulse(true);
        context.setNextPulse(LoaderContext.PILOT_MAX + 1L);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getLastUnclassifiedPulse().longValue());
        assertTrue(context.isCalledCompleteDataBlock());

        // 7: the current pulse was a tail pulse
        context.resetFields();
        context.setCurrentPulse(LoaderContext.TAIL);
        context.setHasNextPulse(true);
        context.setNextPulse(LoaderContext.DATA_TOTAL_MAX + 1L);
        instance = LoaderState.GET_DATA;
        expResult = LoaderState.INITIAL;
        result = instance.nextState(context);
        assertEquals(expResult, result);
        assertEquals(context.getCurrentPulse(), context.getTailLength());
        assertTrue(context.isCalledCompleteDataBlock());
    }

    /**
     * Test of nextState method, of class LoaderState.
     */
    @Test
    public void testEndLoader() {
        System.out.println("nextState");
        MockLoaderContext context = new MockLoaderContext();

        INITIAL.endLoader(context);
        assertTrue(context.isCalledCompletePulseBlock());
        assertFalse(context.isLastIsPilot());
        
        context.resetFields();
        FIND_PILOT.endLoader(context);
        assertTrue(context.isCalledCompletePulseBlock());
        assertFalse(context.isLastIsPilot());
        
        context.resetFields();
        FIND_SYNC1.endLoader(context);
        assertTrue(context.isCalledCompletePulseBlock());
        assertFalse(context.isLastIsPilot());
        
        context.resetFields();
        GET_SYNC2.endLoader(context);
        assertTrue(context.isCalledCompletePulseBlock());
        assertTrue(context.isLastIsPilot());
        
        context.resetFields();
        GET_DATA.endLoader(context);
        assertTrue(context.isCalledCompleteDataBlock());
    }
    
}
