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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class LoaderContextImplTest {
    
    public LoaderContextImplTest() {
    }
    
    /**
     * Test of buildPZXTapeList method, of class LoaderContextImpl.
     */
    @Test
    public void testBuildPZXTapeList() {
        System.out.println("buildPZXTapeList");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        ArrayList<PZXBlock> expResult = null;
        List<PZXBlock> result = LoaderContextImpl.buildPZXTapeList(pulseList);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSync1Length method, of class LoaderContextImpl.
     */
    @Test
    public void testGetSync1Length() {
        System.out.println("getSync1Length");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync1(100.0);
        double expResult = 100.0;
        double result = instance.getSync1Length();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getSync2Length method, of class LoaderContextImpl.
     */
    @Test
    public void testGetSync2Length() {
        System.out.println("getSync2Length");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync2(100.0);
        double expResult = 100.0;
        double result = instance.getSync2Length();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of addZeroPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddZeroPulse() {
        System.out.println("addZeroPulse");
        Double firstPulseLength = 50.0;
        Double secondPulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addZeroPulse(firstPulseLength, secondPulseLength);

        List<Double> result2 = instance.getPulseLengths();
        List<Double> expResult2 = Arrays.asList(50.0, 50.0);
        assertArrayEquals("Check current block pulses recorded", expResult2.toArray(), result2.toArray());

        List<Double> result3 = instance.getZeroPulses();
        assertArrayEquals("Check zero pulses recorded", expResult2.toArray(), result3.toArray());

        byte result4 = instance.getCurrentByte();
        assertEquals("Check bit has been handled", (byte)0x00, result4);
    }

    /**
     * Test of addBit method, of class LoaderContextImpl.
     */
    @Test
    public void testAddBit() {
        System.out.println("addBit");
        int bit = 0;
        LoaderContextImpl instance = null;
        instance.addBit(bit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testResetBlock() {
        System.out.println("resetBlock");
        LoaderContextImpl instance = null;
        instance.resetBlock();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addOnePulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddOnePulse() {
        System.out.println("addOnePulse");
        Double firstPulseLength = 50.0;
        Double secondPulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addOnePulse(firstPulseLength, secondPulseLength);

        List<Double> result2 = instance.getPulseLengths();
        List<Double> expResult2 = Arrays.asList(50.0, 50.0);
        assertArrayEquals("Check current block pulses recorded", expResult2.toArray(), result2.toArray());

        List<Double> result3 = instance.getOnePulses();
        assertArrayEquals("Check one pulses recorded", expResult2.toArray(), result3.toArray());

        byte result4 = instance.getCurrentByte();
        assertEquals("Check bit has been handled", (byte)0x01, result4);
    }

    /**
     * Test of completePulseBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testCompletePulseBlock() {
        System.out.println("completePulseBlock");
        boolean isPilot = false;
        LoaderContextImpl instance = null;
        instance.completePulseBlock(isPilot);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPilotPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddPilotPulse() {
        System.out.println("addPilotPulse");
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addPilotPulse(pulseLength);

        List<Double> result = instance.getPulseLengths();
        List<Double> expResult = Arrays.asList(50.0);
        assertArrayEquals("Check current block pulse recorded", expResult.toArray(), result.toArray());

        List<Double> result2 = instance.getPilotPulses();
        List<Double> expResult2 = Arrays.asList(50.0);
        assertArrayEquals("Check pilot pulse recorded", expResult2.toArray(), result2.toArray());
    }

    /**
     * Test of completeDataBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testCompleteDataBlock() {
        System.out.println("completeDataBlock");
        LoaderContextImpl instance = null;
        instance.completeDataBlock();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addUnclassifiedPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddUnclassifiedPulse() {
        System.out.println("addUnclassifiedPulse");
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addUnclassifiedPulse(pulseLength);

        List<Double> result = instance.getPulseLengths();
        List<Double> expResult = Arrays.asList(50.0);
        assertArrayEquals("Check current block pulse recorded", expResult.toArray(), result.toArray());
    }

    /**
     * Test of revertCurrentBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testRevertCurrentBlock() {
        System.out.println("revertCurrentBlock");
        LoaderContextImpl instance = null;
        instance.revertCurrentBlock();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumPilotPulses method, of class LoaderContextImpl.
     */
    @Test
    public void testGetNumPilotPulses() {
        System.out.println("getNumPilotPulses");
        
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addPilotPulse(pulseLength);

        int expResult = 1;
        int result = instance.getNumPilotPulses();
        assertEquals(expResult, result);
    }

    /**
     * Test of addSync1 method, of class LoaderContextImpl.
     */
    @Test
    public void testAddSync1() {
        System.out.println("addSync1");

        // addSync1 has two effects, filing the sync1 pulse and adding the pulse
        // to the pulse list
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync1(100.0);
        double expResult = 100.0;
        double result = instance.getSync1Length();
        assertEquals(expResult, result, 0.0);
        
        List<Double> result2 = instance.getPulseLengths();
        List<Double> expResult2 = Arrays.asList(100.0);
        assertArrayEquals(expResult2.toArray(), result2.toArray());
    }

    /**
     * Test of addSync2 method, of class LoaderContextImpl.
     */
    @Test
    public void testAddSync2() {
        System.out.println("addSync2");

        // addSync1 has two effects, filing the sync1 pulse and adding the pulse
        // to the pulse list
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync2(100.0);
        double expResult = 100.0;
        double result = instance.getSync2Length();
        assertEquals(expResult, result, 0.0);
        
        List<Double> result2 = instance.getPulseLengths();
        List<Double> expResult2 = Arrays.asList(100.0);
        assertArrayEquals(expResult2.toArray(), result2.toArray());
    }

    /**
     * Test of setTailLength method, of class LoaderContextImpl.
     */
    @Test
    public void testSetTailLength() {
        System.out.println("setTailLength");
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.setTailLength(pulseLength);
        double expResult = 50.0;
        double result = instance.getTailLength();
        assertEquals(expResult, result, 0.0);
        
        List<Double> result2 = instance.getPulseLengths();
        List<Double> expResult2 = Arrays.asList(50.0);
        assertArrayEquals(expResult2.toArray(), result2.toArray());
    }

    /**
     * Test of getTailLength method, of class LoaderContextImpl.
     */
    @Test
    public void testGetTailLength() {
        System.out.println("getTailLength");
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.setTailLength(pulseLength);
        double expResult = 50.0;
        double result = instance.getTailLength();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of hasNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testHasNextPulse() {
        System.out.println("hasNextPulse");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        boolean expResult = true;
        boolean result = instance.hasNextPulse();
        assertEquals("Check hasNextPulse is true when there is another pulse", expResult, result);
        
        instance.getNextPulse();
        expResult = false;
        result = instance.hasNextPulse();
        assertEquals("Check hasNextPulse is false when there is not another pulse", expResult, result);
    }

    /**
     * Test of peekNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testPeekNextPulse() {
        System.out.println("peekNextPulse");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        double expResult = 200.0;
        double result = instance.peekNextPulse();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getCurrentPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testGetCurrentPulse() {
        System.out.println("getCurrentPulse");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.getNextPulse();
        double expResult = 200.0;
        double result = instance.getCurrentPulse();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getCurrentPulseLevel method, of class LoaderContextImpl.
     */
    @Test
    public void testGetCurrentPulseLevel() {
        System.out.println("getCurrentPulseLevel");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.getNextPulse();
        int expResult = 1;
        int result = instance.getCurrentPulseLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testGetNextPulse() {
        System.out.println("getNextPulse");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        double expResult = 200.0;
        double result = instance.getNextPulse();
        assertEquals(expResult, result, 0.0);
    }

}
