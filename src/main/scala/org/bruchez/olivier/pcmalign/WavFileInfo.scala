package org.bruchez.olivier.pcmalign

import java.nio.file.Path

case class WavFileInfo(file: Path, significantLength: Int)

object WavFileInfo {
  def wavFileInfoAndPcmShorts(wavFile: Path): (WavFileInfo, Array[Short], Array[Short]) = {
    val pcmShortsLeft = Ffmpeg.shortsFromWav(wavFile, LeftChannel).get
    val pcmShortsRight = Ffmpeg.shortsFromWav(wavFile, RightChannel).get
    val wavFileSignificantLength = significantLength(pcmShortsLeft, pcmShortsRight)
    (WavFileInfo(wavFile, wavFileSignificantLength), pcmShortsLeft, pcmShortsRight)
  }

  val DefaultHeadingAndTrailingThreshold = 16

  def headingAndTrailingSilenceLengths(
      pcmShortsLeft: Array[Short],
      pcmShortsRight: Array[Short],
      threshold: Int = DefaultHeadingAndTrailingThreshold
  ): (Int, Int) = {
    assert(pcmShortsLeft.length == pcmShortsRight.length)

    var headingSilenceLength = 0
    while (
      pcmShortsLeft(headingSilenceLength).abs <= threshold &&
      pcmShortsRight(headingSilenceLength).abs <= threshold &&
      headingSilenceLength < pcmShortsLeft.length
    ) {
      headingSilenceLength += 1
    }

    var trailingSilenceLength = 0
    while (
      pcmShortsLeft(pcmShortsLeft.length - trailingSilenceLength - 1).abs <= threshold &&
      pcmShortsRight(pcmShortsRight.length - trailingSilenceLength - 1).abs <= threshold &&
      trailingSilenceLength < pcmShortsLeft.length
    ) {
      trailingSilenceLength += 1
    }

    (headingSilenceLength, trailingSilenceLength)
  }

  // Length without heading and trailing silences
  def significantLength(pcmShortsLeft: Array[Short], pcmShortsRight: Array[Short]): Int = {
    assert(pcmShortsLeft.length == pcmShortsRight.length)

    val (headingSilenceLength, trailingSilenceLength) =
      headingAndTrailingSilenceLengths(pcmShortsLeft, pcmShortsRight)
    pcmShortsLeft.length - (headingSilenceLength + trailingSilenceLength)
  }
}
