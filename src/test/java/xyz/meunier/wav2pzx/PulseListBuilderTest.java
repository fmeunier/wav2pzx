/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.meunier.wav2pzx;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fred
 */
public class PulseListBuilderTest {
    
    // Two 238 t state pulses with a trailing 79 t state pulse
    public int[] samples1 = {0, 0, 0, 255, 255, 255, 0};
    public int[] samples2 = {255, 255, 255, 0, 0, 0, 0, 255};
    public static final float SAMPLE_RATE = (float) 44100.0;
    public static final float MACHINE_HZ = (float) 3500000.0;
    public PulseListBuilder instance1;
    public PulseListBuilder instance2;
    
    @Before
    public void setUp() {
        instance1 = new PulseListBuilder(SAMPLE_RATE, MACHINE_HZ);
        for(int i : samples1) {
            instance1.addSample(i);
        }
        
        // Test 2: Build samples2 should produce first pulse level 1
        instance2 = new PulseListBuilder(SAMPLE_RATE, MACHINE_HZ);
        for(int i : samples2) {
            instance2.addSample(i);
        }
    }
    
    @After
    public void tearDown() {
        instance1 = null;
        instance2 = null;
    }

    /**
     * Test of addSample method, of class PulseListBuilder.
     */
    @Test
    public void testAddSample() {
        System.out.println("addSample");
        int sample = 0;
        boolean gotException = false;
        
        // Test 1: shouldn't be able to add out of range samples
        try {
            instance1.addSample(-1);
        } catch (IllegalArgumentException e) {
            gotException = true;
        }
        assertTrue(gotException);
        
        gotException = false;
        try {
            instance1.addSample(256);
        } catch (IllegalArgumentException e) {
            gotException = true;
        }
        assertTrue(gotException);

        // Test 2: shouldn't be able to add a sample to a completed list
        instance1.build();
        gotException = false;
        try {
            instance1.addSample(sample);
        } catch (IllegalStateException e) {
            gotException = true;
        }
        assertTrue(gotException);
        
        // Test 3: Should be able to add an in-range sample to an incomplete list with no exception
        gotException = false;
        instance2.addSample(sample);
        assertFalse(gotException);
    }

    /**
     * Test of build method, of class PulseListBuilder.
     */
    @Test
    public void testFinishPulseList() {
        System.out.println("finishPulseList");

    }

    /**
     * Test of getPulseList method, of class PulseListBuilder.
     */
    @Test
    public void testGetPulseList() {
        System.out.println("getPulseList");
        
        PulseList pulseList = instance1.build();
        List<Double> result = pulseList.getPulseLengths();
        
        assertEquals(238.0, result.get(0), 0.1);
        assertEquals(238.0, result.get(1), 0.1);
        assertEquals(79.365, result.get(2), 0.001);
    }

    /**
     * Test of firstPulseLevel method, of class PulseListBuilder.
     */
    @Test
    public void testFirstPulseLevel() {
        System.out.println("firstPulseLevel");

        // Test 1: Build samples1 should produce first pulse level 0
        int expResult = 0;
        PulseList pulseList = instance1.build();
        int result = pulseList.getFirstPulseLevel();
        assertEquals(expResult, result);
        
        // Test 2: Build samples2 should produce first pulse level 1
        expResult = 1;
        pulseList = instance2.build();
        result = pulseList.getFirstPulseLevel();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of firstPulseLevel method, of class PulseListBuilder.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        
        // Test 0: check we get an exception when built with no pulses added
        boolean expResultBool = true;
        boolean resultBool = false;
        PulseListBuilder instance = new PulseListBuilder(SAMPLE_RATE, MACHINE_HZ);
        try {
            instance.build();
            fail("No exception received");
        } catch (IllegalStateException e) {
            resultBool = true;
        }
        assertEquals(expResultBool, resultBool);
        
        
        // Test that builder transitions to complete state when finished
        assertFalse(instance1.isTapeComplete());

        // Test 1: Check we get a PulseList when build() is called and some samples have been received
        PulseList pulseList = instance1.build();
        assertNotNull(pulseList);

        assertTrue(instance1.isTapeComplete());
        
        // Validate that final pulse is closed by this method?
        pulseList = instance2.build();
        assertEquals(3, pulseList.getPulseLengths().size());
    }
}
