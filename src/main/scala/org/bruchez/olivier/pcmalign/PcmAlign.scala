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
    println(s"Parsing files in ${directory.toFile.getAbsolutePath}...")

    val wavFiles = FileUtils.allFilesInPath(directory, recursive = true) filter { file =>
      val (_, extensionOption) = FileUtils.baseNameAndExtension(file)
      extensionOption.exists(_.trim.toLowerCase == "wav")
    } sortBy {
      _.toFile.getAbsolutePath
    }

    println()

    println(s"WAV file count: ${wavFiles.size}")
    wavFiles.indices.foreach(i => println(s" (${i + 1}) ${wavFiles(i).toFile.getAbsolutePath}"))
    println()

    val pairCandidatesByDistance =
      (for {
        i <- wavFiles.indices
        j <- i + 1 until wavFiles.size
        distanceWithWavFileInfos = {
          print(s"Comparing files ${i + 1} and ${j + 1}...")
          val distanceWithWavFileInfos =
            DistanceWithWavFileInfos(firstWavFile = wavFiles(i), secondWavFile = wavFiles(j))
          println(s" ${distanceWithWavFileInfos.distance.asString}")
          distanceWithWavFileInfos
        }
        if distanceWithWavFileInfos.distance.aligned
      } yield distanceWithWavFileInfos).sortBy(_.distance.averageOfAbsoluteDifferences)

    println()

    println(s"WAV file pair candidate count: ${pairCandidatesByDistance.size}")

    val bestFiles =
      for (distanceWithWavFileInfos <- pairCandidatesByDistance) yield {
        println(
          s" - '${distanceWithWavFileInfos.firstWavFileInfo.file.toFile.getName}' vs " +
            s"'${distanceWithWavFileInfos.secondWavFileInfo.file.toFile.getName}': " +
            s"${distanceWithWavFileInfos.distance.asString}"
        )

        if (
          distanceWithWavFileInfos.firstWavFileInfo.significantLength > distanceWithWavFileInfos.secondWavFileInfo.significantLength
        ) {
          val extra =
            distanceWithWavFileInfos.firstWavFileInfo.significantLength - distanceWithWavFileInfos.secondWavFileInfo.significantLength
          println(
            s"    -> favoring '${distanceWithWavFileInfos.firstWavFileInfo.file.toFile.getName}' since it is longer by $extra samples"
          )
          distanceWithWavFileInfos.firstWavFileInfo
        } else {
          val extra =
            distanceWithWavFileInfos.secondWavFileInfo.significantLength - distanceWithWavFileInfos.firstWavFileInfo.significantLength
          println(
            s"    -> favoring '${distanceWithWavFileInfos.secondWavFileInfo.file.toFile.getName}' since it is longer by $extra samples"
          )
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
