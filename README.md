# wav2pzx
Convert ZX Spectrum tape recordings to the .PZX format

# Overview
The Sinclair ZX Spectrum and related computers primarily stored their software on audio cassette tapes. While most emulators for these machines support the TAP and TZX file formats to efficiently store that software, they are either limited in what software is supported (TAP) or overly complex to support (TZX).

[The PZX format](http://zxds.raxoft.cz/pzx.html) is a more recent attempt to produce a flexible format that can support all software produced for the ZX Spectrum and related machines but is relatively simple to encode and support.

This program translates a recording of a ZX Spectrum tape in WAV format (preferably a mono 8-bit file with maximum volume without clipping the samples) into a PZX file, recognising the standard Spectrum files saved by the ROM routines and preserving any data it doesn't directly support.

It currently expects to be run from the command line and has the following arguments:

    $ java -jar wav2pzx-1.0.jar <infile.wav> <outfile.pzx>
