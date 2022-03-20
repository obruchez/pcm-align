# pcm-align

[![Scala CI](https://github.com/obruchez/pcm-align/actions/workflows/scala.yml/badge.svg)](https://github.com/obruchez/pcm-align/actions/workflows/scala.yml)

Test if WAV files captured from DAT tapes are aligned (i.e. more or less the same). This is a project I needed for a very specific case. My old Sony TCD-D100 portable DAT recorder has an S/PDIF output, but there's some digital noise on the 4 least significant bits (random, not noticeable, completely acceptable in my case). This is something I noticed with my current hardware (2019), as well as a very long time ago with my old soundcard. The problem probably comes from the Sony recorder. Another problem is that I want to automatically detect drops (i.e. short bursts of lost data), if they occur. So the idea is that I will transfer one DAT tape several times. Then, I want to automatically align the resulting WAV files and determine whether some data was lost in one WAV file or another. The algorithm I use for the computation of the best shift/alignment is neither fast nor memory-efficient (WAV files are loaded into memory!). But it works.

Example of output:
```
$ ./pcmalign.sh /DATs
Parsing files in /DATs...

WAV file count: 5
 (1) /DATs/recording1.wav
 (2) /DATs/recording2.wav
 (3) /DATs/recording3.wav
 (4) /DATs/recording4.wav
 (5) /DATs/recording5.wav

Comparing files 1 and 2... avg diff = 138.221, percentile(99.99) = 5678, aligned = false
Comparing files 1 and 3... avg diff = 222.371, percentile(99.99) = 19445, aligned = false
Comparing files 1 and 4... avg diff = 222.146, percentile(99.99) = 19446, aligned = false
Comparing files 1 and 5... avg diff = 222.365, percentile(99.99) = 19445, aligned = false
Comparing files 2 and 3... avg diff = 52.559, percentile(99.99) = 5868, aligned = false
Comparing files 2 and 4... avg diff = 29.272, percentile(99.99) = 5556, aligned = false
Comparing files 2 and 5... avg diff = 52.562, percentile(99.99) = 5868, aligned = false
Comparing files 3 and 4... avg diff = 25.606, percentile(99.99) = 3867, aligned = false
Comparing files 3 and 5... avg diff = 2.346, percentile(99.99) = 11, aligned = true
Comparing files 4 and 5... avg diff = 24.680, percentile(99.99) = 3827, aligned = false

WAV file pair candidate count: 1
 - 'recording3.wav' vs 'recording5.wav': avg diff = 2.346, percentile(99.99) = 11, aligned = true
    -> favoring 'recording3.wav' since it is longer by 1896 samples
```