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
    
    private static final double TOLERANCE = 0.001;

	public LoaderContextImplTest() {
    }
    
    /**
     * Test of buildPZXTapeList method, of class LoaderContextImpl.
     */
    @Test
    public void testBuildPZXTapeList() {
        System.out.println("buildPZXTapeList");
        List<Double> sourcePulses = Arrays.asList(200.0, 200.0, 200.0);
		PulseList pulseList = new PulseList(sourcePulses, 1);
        List<PZXBlock> result = LoaderContextImpl.buildPZXTapeList(pulseList);
        List<Double> pulses = new ArrayList<>();
        result.stream().forEach((b) -> {pulses.addAll(b.getPulses());});
        
        assertEquals("Pulses from generated tape should match source", sourcePulses, pulses);
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
        assertEquals(expResult, result, TOLERANCE);
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
        assertEquals(expResult, result, TOLERANCE);
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
        int bit = 1;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        // add 8 bits to add an entry to data collection, plus one to check current byte
        for(int i = 0; i < 9; i++) {
        	instance.addBit(bit);
        }
        
        List<Byte> expResult = Arrays.asList((byte)0xff);
        List<Byte> result = instance.getData();
        
        assertArrayEquals("Check byte was generated", expResult.toArray(), result.toArray());
        
        byte result2 = instance.getCurrentByte();
        assertEquals("Check bit has been handled", (byte)0x01, result2);
    }

    /**
     * Test of resetBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testResetBlock() {
        System.out.println("resetBlock");
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);

        Double pulseLength = 50.0;
        instance.addPilotPulse(pulseLength);
        instance.addZeroPulse(pulseLength, pulseLength);
        instance.addOnePulse(pulseLength, pulseLength);
        // add 8 bits to add an entry to data collection
        for(int i = 0; i < 8; i++) {
        	instance.addBit(1);
        }
        instance.addSync1(pulseLength);
        instance.addSync2(pulseLength);
        instance.setTailLength(pulseLength);
        
        instance.resetBlock();
        
        checkBlockIsReset(instance);
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
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(pulseLength), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addUnclassifiedPulse(instance.getNextPulse());
        int numBlocksStart = instance.getPZXTapeList().size();

        // Add a block with the pulse
        instance.completePulseBlock(false);

        assertTrue("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty());

        List<PZXBlock> blockList = instance.getPZXTapeList();
        
        assertTrue("Check one block was added", blockList.size() - numBlocksStart == 1);
        
        // Check block has been created and added to the list as proper type
        // Non-pilot
        PZXBlock pzxBlock = blockList.get(numBlocksStart);
		assertTrue(pzxBlock instanceof PZXPulseBlock);
		
		PZXPulseBlock pzxPulseBlock = (PZXPulseBlock)pzxBlock;
		
		// Check pulse in block and initial level is as expected
		assertEquals(pzxPulseBlock.getFirstPulseLevel(), 1);
		List<Double> pulses = pzxPulseBlock.getPulses();
		assertEquals(pulses.size(), 1);
		assertEquals(pulseLength, pulses.get(0), TOLERANCE);
        
        pulseList = new PulseList(Arrays.asList(pulseLength, pulseLength, pulseLength), 0);
        instance = new LoaderContextImpl(pulseList);

        // Pilot
        instance.addPilotPulse(instance.getNextPulse());
        instance.addSync1(instance.getNextPulse());
        instance.addSync2(instance.getNextPulse());
        
        numBlocksStart = instance.getPZXTapeList().size();

        // Add a block with the pulse
        instance.completePulseBlock(true);

        blockList = instance.getPZXTapeList();
        
        assertTrue("Check one block was added", blockList.size() - numBlocksStart == 1);
        
        // Check block has been created and added to the list as proper type
        // Non-pilot
        pzxBlock = blockList.get(numBlocksStart);
        assertTrue(pzxBlock instanceof PZXPilotBlock);
        
        PZXPilotBlock pzxPilotBlock = (PZXPilotBlock)pzxBlock;

		// Check pilot block details
		assertEquals(pzxPilotBlock.getFirstPulseLevel(), 0);
		pulses = pzxPilotBlock.getPulses();
		assertEquals(pulses.size(), 3);
		assertEquals(pulseLength, pulses.get(0), TOLERANCE);
		assertEquals(pulseLength, pulses.get(1), TOLERANCE);
		assertEquals(pulseLength, pulses.get(2), TOLERANCE);
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
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        int numBlocksStart = instance.getPZXTapeList().size();

        // if there were no pulses completeDataBlock() has no effect
        instance.completeDataBlock();

        List<PZXBlock> blockList = instance.getPZXTapeList();
        
        assertTrue("Check no block was added when no pulses were added", blockList.size() - numBlocksStart == 0);
        
        // Add 8 one bits and a tail
        for(int i = 0; i < 8; i++) {
        	instance.addOnePulse((double)LoaderContext.ONE, (double)LoaderContext.ONE);
        }
        
        instance.completeDataBlock();
        
        assertTrue("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty());

        blockList = instance.getPZXTapeList();
        
        assertTrue("Check one block was added", blockList.size() - numBlocksStart == 1);
        
        // Check block has been created and added to the list as proper type
        // Non-pilot
        PZXBlock pzxBlock = blockList.get(numBlocksStart);
		assertSame(pzxBlock.getClass(), PZXDataBlock.class);
        
		PZXDataBlock pzxDataBlock = (PZXDataBlock)pzxBlock;
		
		// Check pulse in block and initial level is as expected
		assertEquals(pzxDataBlock.getFirstPulseLevel(), 0);

		List<Double> pulses = pzxDataBlock.getPulses();
		assertEquals(pulses.size(), 16);
		pulses.stream().forEach((d) -> {
			assertEquals((double)LoaderContext.ONE, d, TOLERANCE);
		});

        // if there is no partially accumulated byte the num bits in last byte should be 8
		assertEquals("Check last byte had eight bits", 8, pzxDataBlock.getNumBitsInLastByte());
		
        checkBlockIsReset(instance);

        assertEquals(1, pzxDataBlock.getData().length);
        assertEquals((byte)0xff, pzxDataBlock.getData()[0]);

        // any partially accumulated byte should be added to the data collection
    	instance.addOnePulse((double)LoaderContext.ONE, (double)LoaderContext.ONE);
    	
        numBlocksStart = instance.getPZXTapeList().size();

        instance.completeDataBlock();
        
        assertTrue("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty());

        blockList = instance.getPZXTapeList();
        
        assertTrue("Check one block was added", blockList.size() - numBlocksStart == 1);

        pzxBlock = blockList.get(numBlocksStart);
		assertTrue(pzxBlock instanceof PZXDataBlock);
        
		pzxDataBlock = (PZXDataBlock)pzxBlock;
		
        assertEquals("Check one byte was added", 1, pzxDataBlock.getData().length);
		assertEquals("Check correct value of partial byte", (byte)0x01, pzxDataBlock.getData()[0]);
		assertEquals("Check last byte had only one bit", 1, pzxDataBlock.getNumBitsInLastByte());
     
        checkBlockIsReset(instance);
    }

	private void checkBlockIsReset(LoaderContextImpl instance) {
        double expResult = 0.0;
        double result = instance.getSync1Length();
        assertEquals("Check sync1 has been reset", expResult, result, TOLERANCE);

        result = instance.getSync2Length();
        assertEquals("Check sync2 has been reset", expResult, result, TOLERANCE);

        result = instance.getTailLength();
        assertEquals("Check tail length has been reset", expResult, result, TOLERANCE);
        
        byte result2 = instance.getCurrentByte();
        assertEquals("Check bit has been reset", (byte)0x00, result2);

        assertEquals("Check number of bits in current byte is 0", 0, instance.getNumBitsInCurrentByte());
        
        List<Double> result3 = instance.getPilotPulses();
        assertTrue("Check no pilot pulse recorded", result3.isEmpty());

        result3 = instance.getZeroPulses();
        assertTrue("Check no zero pulses recorded", result3.isEmpty());

        result3 = instance.getOnePulses();
        assertTrue("Check no one pulses recorded", result3.isEmpty());
        
        assertTrue("Check no data recorded", instance.getData().isEmpty());
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
        Double pulseLength = 50.0;
        PulseList pulseList = new PulseList(Arrays.asList(200.0), 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addUnclassifiedPulse(pulseLength);
        // Add a block with the pulse
        instance.completePulseBlock(false);
        int numBlocksStart = instance.getPZXTapeList().size();
        assertTrue(instance.getPulseLengths().isEmpty());
        instance.revertCurrentBlock();
        assertEquals(numBlocksStart - 1, instance.getPZXTapeList().size());
        assertFalse(instance.getPulseLengths().isEmpty());
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
        assertEquals(expResult, result, TOLERANCE);
        
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
        assertEquals(expResult, result, TOLERANCE);
        
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
        assertEquals(expResult, result, TOLERANCE);
        
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
        assertEquals(expResult, result, TOLERANCE);
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
        assertEquals(expResult, result, TOLERANCE);
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
        assertEquals(expResult, result, TOLERANCE);
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
        assertEquals(expResult, result, TOLERANCE);
    }

}
