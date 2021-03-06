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
package xyz.meunier.wav2pzx.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import xyz.meunier.wav2pzx.pulselist.PulseList;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.primitives.Longs.asList;
import static java.lang.Long.valueOf;
import static xyz.meunier.wav2pzx.blocks.PZXEncodeUtils.*;
import static xyz.meunier.wav2pzx.romdecoder.LoaderContext.*;

/**
 * Represents a PZX data block (DATA). Stores the decoded bytes found on the tape.
 * @author Fredrick Meunier
 */
public final class PZXDataBlock implements PZXBlock {
   
    // Spectrum ROM marker of header blocks
    private static final int HEADER_FLAG = 0x00;
    
    // Data length of Spectrum header blocks in bytes
    private static final int HEADER_LENGTH = 19;
    
    // Mask for high bit of a 32 bit integer
    private static final int BIT_32_MASK = 0x80000000;

    // Details of original pulses corresponding to block
    private final PulseList pulses;
    
    // The length of the tail pulse identified in the source file
    private final short tailLength;
    
    // The decoded data for the data block
    private final byte[] data;
    
    // The number of data bits in the block (the last byte may be incomplete)
    private final int numBitsInLastByte;
    
    // The checksum we have calculated for the data in the block
    private final byte calculatedChecksum;
    
    // The checksum from the loaded data block
    private final byte suppliedChecksum;
    
    // Was this block a header or data block?
    private final boolean isHeader;

    // The length of the zero pulse identified in the source file
    private final ImmutableList<Long> zeroPulseLengths;

    // The length of the one pulse identified in the source file
    private final ImmutableList<Long> onePulseLengths;

    /**
     * Constructs a new PZXDataBlock.
     * @param newPulses the original tape pulses that have been decoded into this block
     * @param zeroPulseLengths the lengths of the zero pulses in the block
     * @param onePulseLengths the lengths of the one pulses in the block
     * @param tailLength the length of the tail pulse in the block
     * @param numBitsInLastByte the number of bits used in the last byte of the data collection
     * @param data the decoded data from the tape image
     * @throws NullPointerException if newPulses or data is null
     * @throws IllegalArgumentException if data is empty
     */
    public PZXDataBlock(PulseList newPulses, List<Long> zeroPulseLengths, List<Long> onePulseLengths, long tailLength,
                        int numBitsInLastByte, Collection<Byte> data) {
        checkNotNull(newPulses, "newPulses must not be null");
        checkNotNull(data, "data must not be null");
        checkArgument(!data.isEmpty(), "data array must not be empty");
        this.pulses = newPulses;
        this.zeroPulseLengths = copyOf(zeroPulseLengths);
        this.onePulseLengths = copyOf(onePulseLengths);
        this.tailLength = (short)tailLength;
        this.numBitsInLastByte = numBitsInLastByte;
        this.data = Bytes.toArray(data);
        this.isHeader = this.data.length == HEADER_LENGTH &&
                this.data[0] == HEADER_FLAG;
        this.suppliedChecksum = this.data[this.data.length-1];
        this.calculatedChecksum = calcChecksum();
    }

    /**
     * Constructs a new PZXDataBlock.
     * @param newPulses the original tape pulses that have been decoded into this block
     * @param numBitsInLastByte the number of bits used in the last byte of the data collection
     * @param data the decoded data from the tape image
     * @throws NullPointerException if newPulses or data is null
     * @throws IllegalArgumentException if data is empty
     */
    public PZXDataBlock(PulseList newPulses, int numBitsInLastByte, Collection<Byte> data) {
        this(newPulses, asList(valueOf(ZERO), valueOf(ZERO)), asList(valueOf(ONE), valueOf(ONE)), TAIL,
                numBitsInLastByte, data);
    }

    // Calculates the checksum for the data according to the algorithm in the 
    // Spectrum ROM (XOR all data bytes except the last which holds the saved 
    // checksum)
    private byte calcChecksum() {
        byte checksum = 0;
        if( this.data.length != 0 ) {
            for( int i = 0; i < this.data.length-1; i++ ) {
                checksum ^= this.data[i];
            }
        }
        return checksum;
    }

    @Override
    public byte[] getPZXBlockDiskRepresentation() {
    	/*  DATA - Data block
            -----------------

            offset      type             name  meaning
            0           u32              count bits 0-30 number of bits in the data stream
                                               bit 31 initial pulse level: 0 low 1 high
            4           u16              tail  duration of extra pulse after last bit of the block
            6           u8               p0    number of pulses encoding bit equal to 0.
            7           u8               p1    number of pulses encoding bit equal to 1.
            8           u16[p0]          s0    sequence of pulse durations encoding bit equal to 0.
            8+2*p0      u16[p1]          s1    sequence of pulse durations encoding bit equal to 1.
            8+2*(p0+p1) u8[ceil(bits/8)] data  data stream, see below.

            This block is used to represent binary data using specified sequences of
            pulses. The data bytes are processed bit by bit, most significant bits first.
            Each bit of the data is represented by one of the sequences, s0 if its value
            is 0 and s1 if its value is 1, respectively. Each sequence consists of
            pulses of specified durations, p0 pulses for sequence s0 and p1 pulses for
            sequence s1, respectively.

            The initial pulse level is specified by bit 31 of the count field. For data
            saved with standard ROM routines, it should be always set to high, as
            mentioned in PULS description above. Also note that pulse of zero duration
            may be used to invert the pulse level at start and/or the end of the
            sequence. It would be also possible to use it for pulses longer than 65535 T
            cycles in the middle of the sequence, if it was ever necessary.
        */
    
        ArrayList<Byte> output = new ArrayList<>(12 + data.length);
        
        // bits 0-30 number of bits in the data stream
        // bit 31 initial pulse level: 0 low 1 high
        int count = (data.length - 1) * 8 + numBitsInLastByte;
        
        if( getFirstPulseLevel() == 1 ) {
            count |= BIT_32_MASK;
        }
        
        putUnsignedLittleEndianInt(count, output);
        
        // use standard duration tail pulse after last bit of the block if we found one
        putUnsignedLittleEndianShort(tailLength == 0 ? 0 : tailLength, output);
        
        putUnsignedByte((byte)zeroPulseLengths.size(), output); // number of pulses encoding bit equal to 0.
        putUnsignedByte((byte)onePulseLengths.size(), output); // number of pulses encoding bit equal to 1.
        
        // sequence of pulse durations encoding bit equal to 0.
        putPulseList(zeroPulseLengths, output);

        // sequence of pulse durations encoding bit equal to 1.
        putPulseList(onePulseLengths, output);

        // data stream
        output.addAll(Bytes.asList(data));
        
        return addPZXBlockHeader("DATA", output);
    }

    private void putPulseList(List<Long> pulseLengths, Collection<Byte> output) {
        for (Long pulseLength : pulseLengths) {
            putUnsignedLittleEndianShort(pulseLength.shortValue(), output);
        }
    }

    @Override
    public String getSummary() {
        StringBuilder retval = new StringBuilder();
        retval.append("Data block ended, found ");
        retval.append(data.length);
        retval.append(" bytes\n");
        retval.append(String.format("Flag: 0x%x%n", data[0]));
        retval.append("Type: ");
        retval.append((isHeader ? "Header" : "Data"));
        retval.append("\n");
        if( isHeader ) {
            // FIXME: Should be an enum?
            switch( data[1] ) {
            case 0:
              retval.append("Program: ");
              break;
            case 1:
              retval.append("Number Array: ");
              break;
            case 2:
              retval.append("Character Array: ");
              break;
            case 3:
              retval.append("CODE: ");
              break;
            default:
              retval.append("Unknown: ");
              break;
            }
            retval.append(getSanitisedFilename());
            retval.append("\n");
        }

        if( numBitsInLastByte % 8 != 0 ) {
            retval.append("Error have incomplete last byte (")
                .append(numBitsInLastByte % 8).append(" bits)\n");
        }
        
        retval.append(String.format("Checksum:%s Read: 0x%x Computed: 0x%x%n",
                            (checkChecksum() ? "PASS" : "FAIL"), this.suppliedChecksum, 
                            this.calculatedChecksum ) );
        
        retval.append("Tail pulse:");
        if(tailLength != 0) {
            retval.append(tailLength).append(" tstates, ")
                .append(Math.round((double)tailLength/ TAIL*100.0)).append("% of expected\n");
        } else {
            retval.append(" None\n");
        }

        return retval.toString();
    }

    // FIXME: Should have a custom mapping of Sinclair character encoding to Java characters, but printable US-ASCII is
    // close enough for now
    private String getSanitisedFilename() {
        return new String(data, 2, 10, Charset.forName("US-ASCII") ).replaceAll("[^\\p{Print}]", "?");
    }

    /**
     * @return true if this is a header block, false for a data block
     */
    public boolean isHeader() {
        return isHeader;
    }

    /**
     * @return true if the calculated checksum matches the one found in the tape block
     */
    public boolean checkChecksum() {
        boolean retval = false;
        if( data.length != 0 ) {
            retval = this.calculatedChecksum == this.suppliedChecksum;
        }

        return retval;
    }

    @Override
    public String toString() {
        return "PZXDataBlock{" + pulses.toString() + ", zeroPulseLengths=" + zeroPulseLengths + ", onePulseLengths="
                + onePulseLengths + ", tailLength=" + tailLength + ", numBitsInLastByte=" + numBitsInLastByte
                + ", calculatedChecksum=" + String.format("0x%x", calculatedChecksum) + ", suppliedChecksum="
                + String.format("0x%x", suppliedChecksum) + ", isHeader=" + isHeader + ", data.length="
                + data.length + '}';
    }

    @Override
    public int hashCode() {
        int result = pulses.hashCode();
        result = 31 * result + (int) tailLength;
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + numBitsInLastByte;
        result = 31 * result + (int) calculatedChecksum;
        result = 31 * result + (int) suppliedChecksum;
        result = 31 * result + (isHeader ? 1 : 0);
        result = 31 * result + zeroPulseLengths.hashCode();
        result = 31 * result + onePulseLengths.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PZXDataBlock that = (PZXDataBlock) o;

        if (tailLength != that.tailLength) return false;
        if (numBitsInLastByte != that.numBitsInLastByte) return false;
        if (calculatedChecksum != that.calculatedChecksum) return false;
        if (suppliedChecksum != that.suppliedChecksum) return false;
        if (isHeader != that.isHeader) return false;
        if (!pulses.equals(that.pulses)) return false;
        if (!Arrays.equals(data, that.data)) return false;
        if (!zeroPulseLengths.equals(that.zeroPulseLengths)) return false;
        return onePulseLengths.equals(that.onePulseLengths);
    }

    /**
	 * @return the number of bits of data in the last data block byte
	 */
	public int getNumBitsInLastByte() {
		return numBitsInLastByte;
	}

	/**
	 * @return a copy of the data section of the block
	 */
	public byte[] getData() {
		return Arrays.copyOf(data, data.length);
	}

	@Override
	public List<Long> getPulses() {
		return pulses.getPulseLengths();
	}

	@Override
	public int getFirstPulseLevel() {
		return pulses.getFirstPulseLevel();
	}
    
}
