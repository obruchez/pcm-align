package org.bruchez.olivier.pcmalign

import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Path, Paths}
import scala.util._

object PcmAlign {
  def main(args: Array[String]): Unit = {
    val unitTry =
      for {
        firstPcmShorts <- shortsFromWav(
          Paths.get("/Users/olivierbruchez/Downloads/DAT tests/abdullah3.wav"),
          LeftChannel)
        secondPcmShorts <- shortsFromWav(
          Paths.get("/Users/olivierbruchez/Downloads/DAT tests/abdullah5.wav"),
          LeftChannel)
      } yield {
        println(s"firstPcmShorts -> ${firstPcmShorts.length}")
        println(s"secondPcmShorts -> ${secondPcmShorts.length}")

        val ThirtySecondsInSamples = 44100 * 30

        var minAvg = Double.MaxValue
        var minOffset = 0

        for (offset <- -ThirtySecondsInSamples to ThirtySecondsInSamples) {
          val avg = averageOfAbsoluteDifferences(firstPcmShorts,
                                                 secondPcmShorts,
                                                 offset,
                                                 maxValueCountToSum = ThirtySecondsInSamples)
          //println(s"$offset -> $avg")

          if (avg < minAvg) {
            minAvg = avg
            minOffset = offset
          }
        }

        minOffset
      }

    println(s"unitTry = $unitTry")
  }

  def shortsFromWav(wavFile: Path, stereoChannel: StereoChannel): Try[Array[Short]] =
    Ffmpeg.monoS16lePcmFromWavFile(wavFile, stereoChannel) map { pcmFile =>
      val buffer = Files.readAllBytes(pcmFile)

      val byteBuffer = ByteBuffer.wrap(buffer)
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

      val shorts = new Array[Short](buffer.length / 2)
      byteBuffer.asShortBuffer.get(shorts)

      shorts
    }

  def averageOfAbsoluteDifferences(firstArray: Array[Short],
                                   secondArray: Array[Short],
                                   offsetInFirstArray: Int,
                                   maxValueCountToSum: Int): Double = {
    val (firstArrayToProcess, secondArrayToProcess, positiveOffsetInFirstArray) =
      if (offsetInFirstArray >= 0) {
        (firstArray, secondArray, offsetInFirstArray)
      } else {
        (secondArray, firstArray, -offsetInFirstArray)
      }

    var firstIndex = positiveOffsetInFirstArray
    var secondIndex = 0

    var sum = 0.0

    while (firstIndex < firstArrayToProcess.length && secondIndex < secondArrayToProcess.length && secondIndex < maxValueCountToSum) {
      sum += math.abs(firstArrayToProcess(firstIndex) - secondArrayToProcess(secondIndex)).toDouble
      firstIndex += 1
      secondIndex += 1
    }

    sum / secondIndex
  }
}
