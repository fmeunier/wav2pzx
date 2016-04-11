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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * State machine that drives the processing of a sequence of pulses, recognising
 * pilot and data blocks.
 * <p>
 * State is all extrinsic and stored in a supplied LoaderContext object.
 * @author Fredrick Meunier
 */
public enum LoaderState {

    /**
     * This is the initial state of the loader, searching for a PILOT tone.
     * Generates a pulse sequence block.
     */
    INITIAL {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if( PulseUtils.equalWithinResolution(context.getCurrentPulse(), LoaderContext.PILOT_LENGTH, context.getResolution()) ) {
                // Close current pulse block - note that this may be empty!
                context.completePulseBlock(false);

                // Put pulse into new pulse block and pilot pulse stats
                context.addPilotPulse(context.getCurrentPulse());
                
                logTransition(context.getCurrentPulse(), INITIAL, FIND_PILOT);
                return FIND_PILOT;
            } else {
                // Put pulse into current block
                context.addUnclassifiedPulse(context.getCurrentPulse());

                return INITIAL;
            }
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completePulseBlock(false);
        }
    },

    /**
     * The parser is in this state if it is currently accumulating pilot pulses.
     */
    FIND_PILOT {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if( !PulseUtils.equalWithinResolution(context.getCurrentPulse(), LoaderContext.PILOT_LENGTH, context.getResolution()) ) {
                // Not really a new pulse sequence, dump pulses into existing block
                context.revertCurrentBlock();
                context.addUnclassifiedPulse(context.getCurrentPulse());
                
                logTransition(context.getCurrentPulse(), FIND_PILOT, INITIAL);
                return INITIAL;
            }
                
            // FIXME: this seems too generous to pulses too short to count as a
            // conventional pilot tone? Should also have PILOT_MIN? Looks like
            // the original ROM routines are not concerned and rely on loading
            // data saved in the expected format
            
            context.addPilotPulse( context.getCurrentPulse() );

            if( context.getNumPilotPulses() >= LoaderContext.MIN_PILOT_COUNT ) {
                logTransition(context.getCurrentPulse(), FIND_PILOT, FIND_SYNC1);
                return FIND_SYNC1;
            }
            
            return FIND_PILOT;
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completePulseBlock(false);
        }
    },
    
    /**
     * Look for the SYNC1 pulse, when found transition to SYNC2 or if not found
     * transition back to looking for PILOT pulses.
     */
    FIND_SYNC1 {
        @Override
        public LoaderState nextState(LoaderContext context) {
            // look for pulse no longer than SYNC_TOTAL_MAX, but less than SYNC1_MAX
            if( context.getCurrentPulse() > LoaderContext.SYNC_TOTAL_MAX ) {
                // Not really a new pulse sequence, dump pulses into existing block
                context.revertCurrentBlock();
                context.addUnclassifiedPulse(context.getCurrentPulse());
                
                // go back to looking for pilot tones, but re-process this pulse
                // in the new state
                logTransition(context.getCurrentPulse(), FIND_SYNC1, INITIAL);
                return INITIAL.nextState(context);
            } else if ( context.getCurrentPulse() <= LoaderContext.SYNC1_MAX ) {
                // Found the SYNC1 pulse
                context.addSync1(context.getCurrentPulse());

                // get sync2
                logTransition(context.getCurrentPulse(), FIND_SYNC1, GET_SYNC2);
                return GET_SYNC2;
            }
            
            context.addPilotPulse( context.getCurrentPulse() );
            
            return FIND_SYNC1;
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completePulseBlock(false);
        }
    },
    
    /**
     * We have found the SYNC1 pulse so the next pulse should be SYNC2. If not
     * found transition back to looking for PILOT pulses.
     */
    GET_SYNC2 {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if ( context.getSync1Length() + context.getCurrentPulse() < LoaderContext.SYNC_TOTAL_MAX ) {
                // Found the SYNC2 pulse
                context.addSync2(context.getCurrentPulse());
                
                context.completePulseBlock(true);
                
                logTransition(context.getCurrentPulse(), GET_SYNC2, GET_DATA);
                return GET_DATA;
            } else {
                // Not really a new pulse sequence, dump pulses into existing block
                context.revertCurrentBlock();
                context.addUnclassifiedPulse(context.getCurrentPulse());
                
                // go back to looking for pilot tones, but re-process this pulse
                // in the new state
                logTransition(context.getCurrentPulse(), GET_SYNC2, INITIAL);
                return INITIAL.nextState(context);
            }
        }

        @Override
        public void endLoader(LoaderContext context) {
            // keep the suspiciously organised pulse block
            context.completePulseBlock(true);
        }
    },
    
    /**
     * Get the data for a data block and the tail pulse if found. When we can't
     * find any more data bits or a tail pulse, complete the block and head back
     * to looking for PILOT pulses.
     */
    GET_DATA {
        @Override
        public LoaderState nextState(LoaderContext context) {
            // If there is no next pulse, check if this pulse is a tail pulse
            // and add it to this block, if not close this block and go to INITIAL
            // state
            if( !context.hasNextPulse() ) {
                return handleOptionalTailPulse(context);
            }
            
            long nextPulseLength = context.peekNextPulse();
            
            if( PulseUtils.equalWithinResolution(context.getCurrentPulse(), LoaderContext.ZERO, context.getResolution()) &&
            	 PulseUtils.equalWithinResolution(nextPulseLength, LoaderContext.ZERO, context.getResolution())) {
                context.addZeroPulse(context.getCurrentPulse(), nextPulseLength );
                context.getNextPulse(); // Defer side effect of calculating next pulse level
                
                logTransition(context.getCurrentPulse(), GET_DATA, GET_DATA);
                return GET_DATA;
            } else if (PulseUtils.equalWithinResolution(context.getCurrentPulse(), LoaderContext.ONE, context.getResolution()) &&
               	 PulseUtils.equalWithinResolution(nextPulseLength, LoaderContext.ONE, context.getResolution())) {
            	
                context.addOnePulse(context.getCurrentPulse(), nextPulseLength );
                context.getNextPulse(); // Defer side effect of calculating next pulse level
                
                logTransition(context.getCurrentPulse(), GET_DATA, GET_DATA);
                return GET_DATA;
            } else {
                return handleOptionalTailPulse(context);
            }
        }

        /**
         *
         * @param context the value of context
         */
        private LoaderState handleOptionalTailPulse(LoaderContext context) {
            final boolean wasTailPulse = PulseUtils.equalWithinResolution(context.getCurrentPulse(), LoaderContext.TAIL, context.getResolution());
            if( wasTailPulse ) {
                context.setTailLength( context.getCurrentPulse() );
                
                // End block and go back to searching for a pilot as this is no longer a data block
                context.completeDataBlock();
                
                logTransition(context.getCurrentPulse(), GET_DATA, INITIAL);
                return INITIAL;
            }
            
            // End block and go back to searching for a pilot as this is no longer a data block
            context.completeDataBlock();
            
            logTransition(context.getCurrentPulse(), GET_DATA, INITIAL);
            return INITIAL.nextState(context);
        }

        @Override
        public void endLoader(LoaderContext context) {
            context.completeDataBlock();
        }
    };
    
    /**
     * Returns the next state of the state machine depending on the current 
     * state and the next pulse from the file being analysed.
     *
     * @param context the extrinsic state of the Loader
     * @return the LoaderState
     */
    public abstract LoaderState nextState(LoaderContext context);
    
    /**
     * Finalises the current loader as there are no more pulses, flushes any remaining
     * block data to the context.
     * @param context the extrinsic state of the Loader
     */
    public abstract void endLoader(LoaderContext context);
    
    /**
     * Debug message to track state transitions.
     * @param currentPulse the pulse that triggered the transition
     * @param fromState the initial state
     * @param toState the terminal state
     */
    private static void logTransition(Long currentPulse, LoaderState fromState, LoaderState toState) {
        String message = String.format("%12s -> %12s: %d", fromState, toState, currentPulse);
        Logger.getLogger(LoaderState.class.getName()).log(Level.FINE, message);
    }
}
