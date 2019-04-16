package org.bruchez.olivier.pcmalign
import java.nio.file.Path

case class DistanceWithWavFileInfos(distance: Distance,
                                    firstWavFileInfo: WavFileInfo,
                                    secondWavFileInfo: WavFileInfo)

object DistanceWithWavFileInfos {
  def apply(firstWavFile: Path, secondWavFile: Path): DistanceWithWavFileInfos = {
    val (firstWavFileInfo, firstPcmShortsLeft, firstPcmShortsRight) =
      WavFileInfo.wavFileInfoAndPcmShorts(firstWavFile)
    val (secondWavFileInfo, secondPcmShortsLeft, secondPcmShortsRight) =
      WavFileInfo.wavFileInfoAndPcmShorts(secondWavFile)

    val bestAlignmentLeft = Alignment.bestAlignment(firstPcmShortsLeft, secondPcmShortsLeft)._1
    val distanceLeft = Distance(firstPcmShortsLeft, secondPcmShortsLeft, bestAlignmentLeft)

    val bestAlignmentRight = Alignment.bestAlignment(firstPcmShortsRight, secondPcmShortsRight)._1
    val distanceRight = Distance(firstPcmShortsRight, secondPcmShortsRight, bestAlignmentRight)

    if (bestAlignmentLeft != bestAlignmentRight) {
      print()
      println(
        s"Warning: best alignment for left = $bestAlignmentLeft vs right = $bestAlignmentRight")
    }
    //assert(bestAlignmentLeft == bestAlignmentRight)

    val distanceMerged = Distance.merged(distanceLeft, distanceRight)

    DistanceWithWavFileInfos(distanceMerged, firstWavFileInfo, secondWavFileInfo)
  }
}
