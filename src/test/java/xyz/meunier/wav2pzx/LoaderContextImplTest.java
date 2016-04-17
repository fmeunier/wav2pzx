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
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import xyz.meunier.wav2pzx.blocks.PZXBlock;
import xyz.meunier.wav2pzx.blocks.PZXDataBlock;
import xyz.meunier.wav2pzx.blocks.PZXNullBlock;
import xyz.meunier.wav2pzx.blocks.PZXPilotBlock;
import xyz.meunier.wav2pzx.blocks.PZXPulseBlock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class LoaderContextImplTest {
    
    private static final double TOLERANCE = 0.001;

    /**
     * Test of buildPZXTapeList method, of class LoaderContextImpl.
     */
    @Test
    public void testBuildPZXTapeList() {
        List<Long> sourcePulses = Arrays.asList(200L, 200L, 200L);
		PulseList pulseList = new PulseList(sourcePulses, 1, 1);
        List<PZXBlock> result = LoaderContextImpl.buildPZXTapeList(pulseList);
        List<Long> pulses = new ArrayList<>();
        result.stream().forEach((b) -> pulses.addAll(b.getPulses()));
        
        assertThat("Pulses from generated tape should match source", pulses, equalTo(sourcePulses));
    }

    /**
     * Test of getSync1Length method, of class LoaderContextImpl.
     */
    @Test
    public void testGetSync1Length() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync1(100L);
        assertThat(instance.getSync1Length(), is(100L));
    }

    /**
     * Test of getSync2Length method, of class LoaderContextImpl.
     */
    @Test
    public void testGetSync2Length() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync2(100L);
        assertThat(instance.getSync2Length(), is(100L));
    }

    /**
     * Test of addZeroPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddZeroPulse() {
        Long firstPulseLength = 50L;
        Long secondPulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addZeroPulse(firstPulseLength, secondPulseLength);

        List<Long> expResult = Arrays.asList(50L, 50L);
        assertThat("Check current block pulses recorded", instance.getPulseLengths(), equalTo(expResult));

        assertThat("Check zero pulses recorded", instance.getZeroPulses(), equalTo(expResult));

        assertThat("Check bit has been handled", instance.getCurrentByte(), is((byte)0x00));
    }

    /**
     * Test of addBit method, of class LoaderContextImpl.
     */
    @Test
    public void testAddBit() {
        int bit = 1;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        // add 8 bits to add an entry to data collection, plus one to check current byte
        for(int i = 0; i < 9; i++) {
        	instance.addBit(bit);
        }

        assertThat("Check byte was generated", instance.getData(), equalTo(Collections.singletonList((byte)0xff)));

        assertThat("Check bit has been handled", instance.getCurrentByte(), is((byte)0x01));
    }

    /**
     * Test of resetBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testResetBlock() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);

        Long pulseLength = 50L;
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
        Long firstPulseLength = 50L;
        Long secondPulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addOnePulse(firstPulseLength, secondPulseLength);

        List<Long> expResult = Arrays.asList(50L, 50L);
        assertThat("Check current block pulses recorded", instance.getPulseLengths(), equalTo(expResult));

        assertThat("Check one pulses recorded", instance.getOnePulses(), equalTo(expResult));

        assertThat("Check bit has been handled", instance.getCurrentByte(), is((byte)0x01));
    }

    /**
     * Test of completePulseBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testCompletePulseBlock() {
        Long pulseLength = (long) LoaderContext.PILOT_LENGTH;
        PulseList pulseList = new PulseList(Collections.singletonList(pulseLength), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        int numBlocksStart = instance.getPZXTapeList().size();
        
		// Where there are no pulses, completePulseBlock should produce a PZXNullBlock
        instance.completePulseBlock(false);
        
        List<PZXBlock> blockList = instance.getPZXTapeList();

        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));
        
        // Check block has been created and added to the list as proper type
        // Non-pilot
        PZXBlock pzxBlock = blockList.get(numBlocksStart);
		assertThat("Check PZXNullBlock is added when no pulses have been encountered", pzxBlock, instanceOf(PZXNullBlock.class));
        
        instance.addUnclassifiedPulse(instance.getNextPulse());
        numBlocksStart = instance.getPZXTapeList().size();

        // Add a block with the pulse
        instance.completePulseBlock(false);

        assertThat("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty(),  is(true));

        blockList = instance.getPZXTapeList();
        
        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));

        // Check block has been created and added to the list as proper type
        // Non-pilot
        pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXPulseBlock.class));

        PZXPulseBlock pzxPulseBlock = (PZXPulseBlock)pzxBlock;
		
		// Check pulse in block and initial level is as expected
		assertThat(pzxPulseBlock.getFirstPulseLevel(), is(1));
		List<Long> pulses = pzxPulseBlock.getPulses();
        assertThat(pulses, equalTo(Collections.singletonList(pulseLength)));

        pulseList = new PulseList(Arrays.asList(10L, pulseLength, pulseLength), 0, 1);
        instance = new LoaderContextImpl(pulseList);

        // Pulses not sufficiently similar to pilot pulses
        instance.addPilotPulse(instance.getNextPulse());
        instance.addSync1(instance.getNextPulse());
        instance.addSync2(instance.getNextPulse());
        
        numBlocksStart = instance.getPZXTapeList().size();

        // Add a block with the pulse
        instance.completePulseBlock(true);

        blockList = instance.getPZXTapeList();

        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));

        // Check block has been created and added to the list as proper type
        pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXPulseBlock.class));

        pzxPulseBlock = (PZXPulseBlock)pzxBlock;

		// Check pulse block details
        assertThat(pzxPulseBlock.getFirstPulseLevel(), is(0));
		pulses = pzxPulseBlock.getPulses();
        assertThat(pulses, equalTo(Arrays.asList(10L, pulseLength, pulseLength)));

        // Pilot
        pulseList = new PulseList(Arrays.asList(pulseLength, pulseLength, pulseLength), 0, 1);
        instance = new LoaderContextImpl(pulseList);

        instance.addPilotPulse(instance.getNextPulse());
        instance.addSync1(instance.getNextPulse());
        instance.addSync2(instance.getNextPulse());
        
        numBlocksStart = instance.getPZXTapeList().size();

        // Add a block with the pulse
        instance.completePulseBlock(true);

        blockList = instance.getPZXTapeList();

        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));

        // Check block has been created and added to the list as proper type
        // Pilot
        pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXPilotBlock.class));

        PZXPilotBlock pzxPilotBlock = (PZXPilotBlock)pzxBlock;

		// Check pilot block details
		assertThat(pzxPilotBlock.getFirstPulseLevel(), is(0));
		pulses = pzxPilotBlock.getPulses();
        assertThat(pulses, equalTo(Arrays.asList(pulseLength, pulseLength, pulseLength)));
    }

    /**
     * Test of addPilotPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddPilotPulse() {
        System.out.println("addPilotPulse");
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addPilotPulse(pulseLength);

        List<Long> expResult = Collections.singletonList(50L);
        assertThat("Check current block pulse recorded", instance.getPulseLengths(), equalTo(expResult));
        assertThat("Check pilot pulse recorded", instance.getPilotPulses(), equalTo(expResult));
    }

    /**
     * Test of completeDataBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testCompleteDataBlock() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        int numBlocksStart = instance.getPZXTapeList().size();

        // if there were no pulses completeDataBlock() has no effect
        instance.completeDataBlock();

        List<PZXBlock> blockList = instance.getPZXTapeList();

        assertThat("Check no block was added when no pulses were added", blockList.size(), is(numBlocksStart));
        
        // Add 8 one bits and a tail
        for(int i = 0; i < 8; i++) {
        	instance.addOnePulse( (long)LoaderContext.ONE, (long)LoaderContext.ONE);
        }
        
        instance.completeDataBlock();

        assertThat("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty(), is(true));

        blockList = instance.getPZXTapeList();

        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));
        
        // Check block has been created and added to the list as proper type
        // Data
        PZXBlock pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXDataBlock.class));

		PZXDataBlock pzxDataBlock = (PZXDataBlock)pzxBlock;
		
		// Check pulse in block and initial level is as expected
        assertThat(pzxDataBlock.getFirstPulseLevel(), is(0));

		List<Long> pulses = pzxDataBlock.getPulses();
        assertThat(pulses.size(), is(16));
		pulses.stream().forEach((d) -> assertEquals(Long.valueOf(LoaderContext.ONE), d));

        // if there is no partially accumulated byte the num bits in last byte should be 8
        assertThat("Check last byte had eight bits", pzxDataBlock.getNumBitsInLastByte(), is(8));
		
        checkBlockIsReset(instance);

        byte[] expected = {(byte)0xff};
        assertArrayEquals(pzxDataBlock.getData(), expected);

        // any partially accumulated byte should be added to the data collection
    	instance.addOnePulse( (long)LoaderContext.ONE, (long)LoaderContext.ONE);
    	
        numBlocksStart = instance.getPZXTapeList().size();

        instance.completeDataBlock();
        
        assertTrue("Check pulse list is emptied when the block is completed", instance.getPulseLengths().isEmpty());

        blockList = instance.getPZXTapeList();
        
        assertTrue("Check one block was added", blockList.size() - numBlocksStart == 1);

        pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXDataBlock.class));

        pzxDataBlock = (PZXDataBlock)pzxBlock;

        byte[] expected2 = {(byte)0x01};
        assertArrayEquals("Check one byte was added with correct value", pzxDataBlock.getData(), expected2);
		assertThat("Check last byte had only one bit", pzxDataBlock.getNumBitsInLastByte(), is(1));
     
        checkBlockIsReset(instance);
        
        // Check that if we attempt to close a data block with pulses but no data we get a pulse block instead
        pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        instance = new LoaderContextImpl(pulseList);
        numBlocksStart = instance.getPZXTapeList().size();

        // Add one non-data pulse
        instance.addUnclassifiedPulse(instance.getNextPulse());
        
        instance.completeDataBlock();

        blockList = instance.getPZXTapeList();
        
        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));
        
        // Check block has been created and added to the list as proper type
        // Non-pilot
        pzxBlock = blockList.get(numBlocksStart);
        assertThat(pzxBlock, instanceOf(PZXPulseBlock.class));

        PZXPulseBlock pzxPulseBlock = (PZXPulseBlock)pzxBlock;
		
		// Check pulse in block and initial level is as expected
		assertThat(pzxPulseBlock.getFirstPulseLevel(), is(1));
		
		assertThat(pzxPulseBlock.getPulses(), equalTo(Collections.singletonList(200L)));

        checkBlockIsReset(instance);

		// Tests for fallbacks to pulse blocks where zero or one pulse lengths are implausible
        numBlocksStart = instance.getPZXTapeList().size();

        // Add 8 one bits and a tail
        for(int i = 0; i < 8; i++) {
        	instance.addOnePulse( (long)LoaderContext.ZERO, (long)LoaderContext.ZERO );
        }
        
        instance.completeDataBlock();
        
        blockList = instance.getPZXTapeList();
        
        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));

        pzxBlock = blockList.get(numBlocksStart);
        assertThat("Check we get a pulse block when one pulse lengths are implausible", pzxBlock, instanceOf(PZXPulseBlock.class));

        numBlocksStart = instance.getPZXTapeList().size();

        // Add 8 zero bits and a tail
        for(int i = 0; i < 8; i++) {
        	instance.addZeroPulse( (long)LoaderContext.ONE, (long)LoaderContext.ONE );
        }
        
        instance.completeDataBlock();
        
        blockList = instance.getPZXTapeList();
        
        assertThat("Check one block was added", blockList.size(), is(numBlocksStart + 1));

        pzxBlock = blockList.get(numBlocksStart);
        assertThat("Check we get a pulse block when zero pulse lengths are implausible", pzxBlock, instanceOf(PZXPulseBlock.class));
    }

	private void checkBlockIsReset(LoaderContextImpl instance) {
        assertThat("Check sync1 has been reset", instance.getSync1Length(), is(0L));
        assertThat("Check sync2 has been reset", instance.getSync2Length(), is(0L));
        assertThat("Check tail length has been reset", instance.getTailLength(), is(0L));
        assertThat("Check bit has been reset", instance.getCurrentByte(), is((byte)0x00));
        assertThat("Check number of bits in current byte is 0", instance.getNumBitsInCurrentByte(), is(0));
        assertThat("Check no pilot pulse recorded", instance.getPilotPulses().isEmpty(), is(true));
        assertThat("Check no zero pulses recorded", instance.getZeroPulses().isEmpty(), is(true));
        assertThat("Check no one pulses recorded", instance.getOnePulses().isEmpty(), is(true));
        assertThat("Check no data recorded", instance.getData().isEmpty(), is(true));
	}

    /**
     * Test of addUnclassifiedPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testAddUnclassifiedPulse() {
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addUnclassifiedPulse(pulseLength);

        assertThat("Check current block pulse recorded", instance.getPulseLengths(), equalTo(Collections.singletonList(50L)));
    }

    /**
     * Test of revertCurrentBlock method, of class LoaderContextImpl.
     */
    @Test
    public void testRevertCurrentBlock() {
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        int numBlocksStart = instance.getPZXTapeList().size();
        // Reverting an empty list should leave the list unchanged
    	instance.revertCurrentBlock();
        assertThat("Reverting a block when we have not added one should have no effect",
        				instance.getPZXTapeList().size(), is(numBlocksStart));
        instance.addUnclassifiedPulse(pulseLength);
        // Add a block with the pulse
        instance.completePulseBlock(false);
        numBlocksStart = instance.getPZXTapeList().size();
        assertThat(instance.getPulseLengths().isEmpty(), is(true));
        instance.revertCurrentBlock();
        assertThat(instance.getPZXTapeList().size(), is(numBlocksStart - 1));
        assertThat(instance.getPulseLengths().isEmpty(), is(false));
    }

    /**
     * Test of getNumPilotPulses method, of class LoaderContextImpl.
     */
    @Test
    public void testGetNumPilotPulses() {
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addPilotPulse(pulseLength);

        assertThat(instance.getNumPilotPulses(), is(1));
    }

    /**
     * Test of addSync1 method, of class LoaderContextImpl.
     */
    @Test
    public void testAddSync1() {
        // addSync1 has two effects, filing the sync1 pulse and adding the pulse
        // to the pulse list
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync1(100L);

        assertThat(instance.getSync1Length(), is(100L));
        assertThat(instance.getPulseLengths(), equalTo(Collections.singletonList(100L)));
    }

    /**
     * Test of addSync2 method, of class LoaderContextImpl.
     */
    @Test
    public void testAddSync2() {
        // addSync1 has two effects, filing the sync1 pulse and adding the pulse
        // to the pulse list
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.addSync2(100L);

        assertThat(instance.getSync2Length(), is(100L));
        assertThat(instance.getPulseLengths(), equalTo(Collections.singletonList(100L)));
    }

    /**
     * Test of setTailLength method, of class LoaderContextImpl.
     */
    @Test
    public void testSetTailLength() {
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.setTailLength(pulseLength);

        assertThat(instance.getTailLength(), is(50L));
        assertThat(instance.getPulseLengths(), equalTo(Collections.singletonList(50L)));
    }

    /**
     * Test of getTailLength method, of class LoaderContextImpl.
     */
    @Test
    public void testGetTailLength() {
        Long pulseLength = 50L;
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.setTailLength(pulseLength);
        assertThat(instance.getTailLength(), is(50L));
    }

    /**
     * Test of hasNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testHasNextPulse() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        assertThat("Check hasNextPulse is true when there is another pulse", instance.hasNextPulse(), is(true));
        
        instance.getNextPulse();
        assertThat("Check hasNextPulse is false when there is not another pulse", instance.hasNextPulse(), is(false));
    }

    /**
     * Test of peekNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testPeekNextPulse() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        assertThat(instance.peekNextPulse(), is(200L));
    }

    /**
     * Test of getCurrentPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testGetCurrentPulse() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.getNextPulse();
        assertThat(instance.getCurrentPulse(), is(200L));
    }

    /**
     * Test of getCurrentPulseLevel method, of class LoaderContextImpl.
     */
    @Test
    public void testGetCurrentPulseLevel() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        instance.getNextPulse();
        assertThat(instance.getCurrentPulseLevel(), is(1));
    }

    /**
     * Test of getNextPulse method, of class LoaderContextImpl.
     */
    @Test
    public void testGetNextPulse() {
        PulseList pulseList = new PulseList(Collections.singletonList(200L), 1, 1);
        LoaderContextImpl instance = new LoaderContextImpl(pulseList);
        assertThat(instance.getNextPulse(), is(200L));
    }

}
