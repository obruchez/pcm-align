package org.bruchez.olivier.pcmalign
import java.nio.file.Path

case class DistanceWithWavFileInfos(distance: Distance,
                                    firstWavFileInfo: WavFileInfo,
                                    secondWavFileInfo: WavFileInfo)

object DistanceWithWavFileInfos {
  def apply(firstWavFile: Path, secondWavFile: Path): DistanceWithWavFileInfos = {
    val (firstWavFileInfo, firstPcmShorts) = WavFileInfo.wavFileInfoAndPcmShorts(firstWavFile)
    val (secondWavFileInfo, secondPcmShorts) = WavFileInfo.wavFileInfoAndPcmShorts(secondWavFile)

    val bestAlignment = Alignment.bestAlignment(firstPcmShorts, secondPcmShorts)._1
    val distance = Distance(firstPcmShorts, secondPcmShorts, bestAlignment)

    DistanceWithWavFileInfos(distance, firstWavFileInfo, secondWavFileInfo)
  }
}
