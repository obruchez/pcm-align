package org.bruchez.olivier.pcmalign

import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Path, Paths}
//import org.apache.spark.SparkContext
//import org.apache.spark.rdd.RDD
//import org.apache.spark.sql.SparkSession
import scala.util._

object PcmAlign {
  /*
  firstPcmShorts -> 323913804
  secondPcmShorts -> 337595205

  unitTry = Success((-158367,3.8649402872260015))

  + count number of 0, 1, 2, 3, etc. differences Map[Int, Int]
  + trim zeroes left/right => compute effective size (idea = use longest recording)
  + do computation on left channel and then on right (should match)
   */

  val ThirtySecondsInSamples: Int = 44100 * 30

  def main(args: Array[String]): Unit = {
    for {
      firstPcmShorts <- shortsFromWav(
        Paths.get("/Users/olivierbruchez/Downloads/DAT tests/abdullah3.wav"),
        LeftChannel)
      secondPcmShorts <- shortsFromWav(
        Paths.get("/Users/olivierbruchez/Downloads/DAT tests/abdullah5.wav"),
        LeftChannel)
    } {
      println(s"1st file length (samples): ${firstPcmShorts.length}")
      println(s"2nd file length (samples): ${secondPcmShorts.length}")

      println("Algorithm 1")
      val (minOffset1, minAverage1) = time(algorithm1(firstPcmShorts, secondPcmShorts))
      println(s"(minOffset, minAverage) = ($minOffset1, $minAverage1)")

      println("Algorithm 2")
      val (minOffset2, minAverage2) = time(algorithm2(firstPcmShorts, secondPcmShorts))
      println(s"(minOffset, minAverage) = ($minOffset2, $minAverage2)")

      println("Algorithm 1 - 2nd execution")
      val (minOffset4, minAverage4) = time(algorithm1(firstPcmShorts, secondPcmShorts))
      println(s"(minOffset, minAverage) = ($minOffset4, $minAverage4)")

      println("Algorithm 2 - 2nd execution")
      val (minOffset5, minAverage5) = time(algorithm2(firstPcmShorts, secondPcmShorts))
      println(s"(minOffset, minAverage) = ($minOffset5, $minAverage5)")
    }
  }

  def time[T](block: => T): T = {
    val t0 = System.nanoTime()
    val t = block
    val t1 = System.nanoTime()

    val timeInSeconds = (t1 - t0).toDouble / 1e9

    println(s"Time in seconds: $timeInSeconds s")

    t
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

  def algorithm1(firstPcmShorts: Array[Short], secondPcmShorts: Array[Short]): (Int, Double) = {
    val offsetsAndAverages = (-ThirtySecondsInSamples to ThirtySecondsInSamples).par map { offset =>
      val average = averageOfAbsoluteDifferences(firstPcmShorts,
                                                 secondPcmShorts,
                                                 offset,
                                                 maxValueCountToSum = ThirtySecondsInSamples)

      offset -> average
    }

    offsetsAndAverages.minBy(_._2)
  }

  def algorithm2(firstPcmShorts: Array[Short], secondPcmShorts: Array[Short]): (Int, Double) = {
    // @todo
    algorithm1(firstPcmShorts, secondPcmShorts)
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

    var sum = 0L

    while (firstIndex < firstArrayToProcess.length && secondIndex < secondArrayToProcess.length && secondIndex < maxValueCountToSum) {
      sum += math.abs(firstArrayToProcess(firstIndex) - secondArrayToProcess(secondIndex)).toLong
      firstIndex += 1
      secondIndex += 1
    }

    sum.toDouble / secondIndex
  }
}
