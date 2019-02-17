package org.bruchez.olivier.pcmalign

import java.nio.file.Path

case class WavFileInfo(file: Path, significantLength: Int)

object WavFileInfo {
  def wavFileInfoAndPcmShorts(wavFile: Path): (WavFileInfo, Array[Short]) = {
    val pcmShorts = Ffmpeg.shortsFromWav(wavFile, LeftChannel).get
    val wavFileSignificantLength = significantLength(pcmShorts)
    (WavFileInfo(wavFile, wavFileSignificantLength), pcmShorts)
  }

  val DefaultHeadingAndTrailingThreshold = 16

  def headingAndTrailingSilenceLengths(
      pcmShorts: Array[Short],
      threshold: Int = DefaultHeadingAndTrailingThreshold): (Int, Int) = {
    var headingSilenceLength = 0
    while (pcmShorts(headingSilenceLength).abs <= threshold && headingSilenceLength < pcmShorts.length) {
      headingSilenceLength += 1
    }

    var trailingSilenceLength = 0
    while (pcmShorts(pcmShorts.length - trailingSilenceLength - 1).abs <= threshold && trailingSilenceLength < pcmShorts.length) {
      trailingSilenceLength += 1
    }

    (headingSilenceLength, trailingSilenceLength)
  }

  // Length without heading and trailing silences
  def significantLength(pcmShorts: Array[Short]): Int = {
    val (headingSilenceLength, trailingSilenceLength) = headingAndTrailingSilenceLengths(pcmShorts)
    pcmShorts.length - (headingSilenceLength + trailingSilenceLength)
  }
}
