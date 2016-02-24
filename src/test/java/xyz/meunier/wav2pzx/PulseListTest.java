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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fredrick Meunier
 */
public class PulseListTest {
    
    public PulseListTest() {
    }

    /**
     * Test of getPulseLengths method, of class PulseList.
     */
    @Test
    public void testGetPulseLengths() {
        System.out.println("getPulseLengths");
        List<Double> pulses = Arrays.asList(200.0, 300.0);
        PulseList instance = new PulseList(pulses, 1);
        List<Double> expResult = pulses;
        List<Double> result = instance.getPulseLengths();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFirstPulseLevel method, of class PulseList.
     */
    @Test
    public void testGetFirstPulseLevel() {
        System.out.println("getFirstPulseLevel");
        List<Double> pulses = Arrays.asList(200.0, 300.0);
        PulseList instance = new PulseList(pulses, 0);
        int expResult = 0;
        int result = instance.getFirstPulseLevel();
        assertEquals(expResult, result);

        instance = new PulseList(pulses, 1);
        expResult = 1;
        result = instance.getFirstPulseLevel();
        assertEquals(expResult, result);
    }
    
}
