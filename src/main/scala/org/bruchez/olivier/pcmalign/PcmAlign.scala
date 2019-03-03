package org.bruchez.olivier.pcmalign

import java.nio.file.{Path, Paths}
import scala.util._

object PcmAlign {
  def main(args: Array[String]): Unit = {
    if (args.length == 1) {
      findCorrectWavFileInDirectory(Paths.get(args.head))
    } else {
      println("Usage: PcmAlign directory")
    }
  }

  def findCorrectWavFileInDirectory(directory: Path): Try[Path] = {
    val wavFiles = FileUtils.allFilesInPath(directory, recursive = true) filter { file =>
      val (_, extensionOption) = FileUtils.baseNameAndExtension(file)
      extensionOption.exists(_.trim.toLowerCase == "wav")
    } sortBy {
      _.toFile.getAbsolutePath
    }

    println(s"WAV file count: ${wavFiles.size}")

    val pairCandidatesByDistance =
      (for {
        i <- wavFiles.indices
        j <- i + 1 until wavFiles.size
        distanceWithWavFileInfos = DistanceWithWavFileInfos(firstWavFile = wavFiles(i),
                                                            secondWavFile = wavFiles(j))
        if distanceWithWavFileInfos.distance.aligned
      } yield distanceWithWavFileInfos).sortBy(_.distance.averageOfAbsoluteDifferences)

    println(s"WAV file pair candidate count: ${pairCandidatesByDistance.size}")

    val bestFiles =
      for (distanceWithWavFileInfos <- pairCandidatesByDistance) yield {
        println(
          s" - distance ${distanceWithWavFileInfos.distance.averageOfAbsoluteDifferences} for " +
            s"'${distanceWithWavFileInfos.firstWavFileInfo.file.toFile.getName}' and " +
            s"'${distanceWithWavFileInfos.secondWavFileInfo.file.toFile.getName}'")

        if (distanceWithWavFileInfos.firstWavFileInfo.significantLength > distanceWithWavFileInfos.secondWavFileInfo.significantLength) {
          val extra = distanceWithWavFileInfos.firstWavFileInfo.significantLength - distanceWithWavFileInfos.secondWavFileInfo.significantLength
          println(
            s"    -> favoring '${distanceWithWavFileInfos.firstWavFileInfo.file.toFile.getName}' since it is longer by $extra samples")
          distanceWithWavFileInfos.firstWavFileInfo
        } else {
          val extra = distanceWithWavFileInfos.secondWavFileInfo.significantLength - distanceWithWavFileInfos.firstWavFileInfo.significantLength
          println(
            s"    -> favoring '${distanceWithWavFileInfos.secondWavFileInfo.file.toFile.getName}' since it is longer by $extra samples")
          distanceWithWavFileInfos.secondWavFileInfo
        }
      }

    if (bestFiles.nonEmpty) {
      Success(bestFiles.head.file)
    } else {
      Failure(new Exception(s"No aligned WAV files found among "))
    }
  }

  def wavFilesAligned(firstWavFile: Path, secondWavFile: Path): Try[Boolean] =
    for {
      firstPcmShorts <- Ffmpeg.shortsFromWav(firstWavFile, LeftChannel)
      secondPcmShorts <- Ffmpeg.shortsFromWav(secondWavFile, LeftChannel)
      bestAlignment = Alignment.bestAlignment(firstPcmShorts, secondPcmShorts)._1
    } yield Distance(firstPcmShorts, secondPcmShorts, bestAlignment).aligned
}
