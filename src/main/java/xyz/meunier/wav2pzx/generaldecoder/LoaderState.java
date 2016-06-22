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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * State machine that drives the processing of a sequence of pulses, recognising
 * pilot and data blocks.
 * <p>
 * State is all extrinsic and stored in a supplied LoaderContext object.
 * @author Fredrick Meunier
 */
enum LoaderState {

    /**
     * This is the initial state of the loader, searching for a PILOT tone.
     * Generates a pulse sequence block.
     */
    INITIAL {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if(context.isaPilotCandidate()) {
                // Close current pulse block - note that this may be empty!
                context.completeUnknownPulseBlock();

                // Put pulse into new pulse block and pilot pulse stats
                context.addPilotPulse();
                
                logTransition(INITIAL, FIND_PILOT);
                return FIND_PILOT;
            } else {
                // Put pulse into current block
                context.addUnclassifiedPulse();

                return INITIAL;
            }
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completeUnknownPulseBlock();
        }
    },

    /**
     * The parser is in this state if it is currently accumulating pilot pulses.
     */
    FIND_PILOT {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if( !context.isaPilotCandidate() ) {
                // Not really a new pulse sequence, dump pulses into existing block
                context.revertCurrentBlock();
                context.addUnclassifiedPulse();
                
                logTransition(FIND_PILOT, INITIAL);
                return INITIAL;
            }
            
            context.addPilotPulse();

            if(context.isaMinimumNumberOfPilotPulses()) {
                logTransition(FIND_PILOT, FIND_PILOT_END);
                return FIND_PILOT_END;
            }
            
            return FIND_PILOT;
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completeUnknownPulseBlock();
        }
    },
    
    /**
     * Look for the end of a sequence of PILOT pulses, when found will either have pulses shorter than MIN_PILOT_LENGTH
     * for data so transition to GET_DATA or if greater than MAX_PILOT_LENGTH transition back to looking for PILOT
     * pulses.
     */
    FIND_PILOT_END {
        @Override
        public LoaderState nextState(LoaderContext context) {
            if( context.isaDataCandidate() ) {
                context.completePilotPulseBlock();

                // Found a candidate sync/data pulse, and re-process this pulse in the new state
                logTransition(FIND_PILOT_END, GET_DATA);
                return GET_DATA.nextState(context);
            } else if(context.isTooLongToBeAPilot()) {
                context.completePilotPulseBlock();

                // go back to looking for pilot tones, but re-process this pulse in the new state
                logTransition(FIND_PILOT_END, INITIAL);
                return INITIAL.nextState(context);
            }

            context.addPilotPulse();
            
            return FIND_PILOT_END;
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current pulse block
            context.completePilotPulseBlock();
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
            if(context.isCurrentAndNextPulseTooLongToBeADataCandidate()) {
                return handleOptionalTailPulse(context);
            }

            // Put pulse into current block
            context.addUnclassifiedPulse();

            return GET_DATA;
        }

        @Override
        public void endLoader(LoaderContext context) {
            // Close current data block - TODO: Should check there is a significant size to the block?
            context.completeDataBlock();
        }

        private LoaderState handleOptionalTailPulse(LoaderContext context) {
            final boolean wasTailPulse = context.isaCandidateTailPulse();

            if( wasTailPulse ) {
                context.addUnclassifiedPulse();
            }

            context.completeDataBlock();

            // go back to looking for pilot tones, but re-process this pulse in the new state if we haven't allocated it
            // to this block
            logTransition(GET_DATA, INITIAL);
            return wasTailPulse ? INITIAL : INITIAL.nextState(context);
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
     * @param fromState the initial state
     * @param toState the terminal state
     */
    private static void logTransition(LoaderState fromState, LoaderState toState) {
        String message = String.format("%12s -> %12s", fromState, toState);
        Logger.getLogger(LoaderState.class.getName()).log(Level.FINE, message);
    }
}
