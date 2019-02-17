package org.bruchez.olivier.pcmalign

import java.nio.file.Path

object Alignment {
  // @todo sample rate should be extracted from WAV file
  val OneSecondInSamples: Int = 44100
  val ThirtySecondsInSamples: Int = OneSecondInSamples * 30

  // Adding more zones doesn't seem to help at all
  val DefaultZoneCount = 3

  def bestAlignment(firstPcmShorts: Array[Short],
                    secondPcmShorts: Array[Short],
                    maxOffset: Int = ThirtySecondsInSamples,
                    zoneCount: Int = DefaultZoneCount): (Int, Double) = {
    val smallestArraySize = math.min(firstPcmShorts.length, secondPcmShorts.length)

    val ZoneSize = OneSecondInSamples

    // Skip the first and last 10% of the data
    val start = smallestArraySize / 10
    val end = (9 * smallestArraySize.toLong / 10).toInt - ZoneSize

    // Sample ten zones between 10% and 90%
    val zoneStarts =
      (0 until zoneCount).map(i => (start + (end - start).toLong * i / (zoneCount - 1)).toInt)

    val offsetsAndAverages = (-maxOffset to maxOffset).par map { offset =>
      val average = (zoneStarts map { zoneStart =>
        averageOfAbsoluteDifferences(firstPcmShorts,
                                     secondPcmShorts,
                                     offsetInFirstArray = offset,
                                     compareValuesStartingFrom = zoneStart,
                                     maxValueCountToSum = ZoneSize)
      }).sum / zoneStarts.size

      offset -> average
    }

    offsetsAndAverages.minBy(_._2)
  }

  def averageOfAbsoluteDifferences(firstArray: Array[Short],
                                   secondArray: Array[Short],
                                   offsetInFirstArray: Int,
                                   compareValuesStartingFrom: Int,
                                   maxValueCountToSum: Int): Double = {
    assert(compareValuesStartingFrom >= 0)

    val (firstArrayToProcess, secondArrayToProcess, positiveOffsetInFirstArray) =
      if (offsetInFirstArray >= 0) {
        (firstArray, secondArray, offsetInFirstArray)
      } else {
        (secondArray, firstArray, -offsetInFirstArray)
      }

    var firstIndex = compareValuesStartingFrom + positiveOffsetInFirstArray
    var secondIndex = compareValuesStartingFrom

    var valueCount = 0

    var sum = 0L

    while (firstIndex < firstArrayToProcess.length && secondIndex < secondArrayToProcess.length && valueCount < maxValueCountToSum) {
      sum += math.abs(firstArrayToProcess(firstIndex) - secondArrayToProcess(secondIndex)).toLong
      firstIndex += 1
      secondIndex += 1
      valueCount += 1
    }

    sum.toDouble / valueCount
  }

  def bestAlignmentSlow(firstPcmShorts: Array[Short],
                        secondPcmShorts: Array[Short],
                        maxOffset: Int): (Int, Double) = {
    val offsetsAndAverages = (-maxOffset to maxOffset).par map { offset =>
      val average = averageOfAbsoluteDifferences(firstPcmShorts,
                                                 secondPcmShorts,
                                                 offsetInFirstArray = offset,
                                                 compareValuesStartingFrom = 0,
                                                 maxValueCountToSum = ThirtySecondsInSamples)

      offset -> average
    }

    offsetsAndAverages.minBy(_._2)
  }

  def time[T](block: => T): T = {
    val t0 = System.nanoTime()
    val t = block
    val t1 = System.nanoTime()

    val timeInSeconds = (t1 - t0).toDouble / 1e9

    println(s"Time in seconds: $timeInSeconds s")

    t
  }

  def testAlgorithms(firstWavFile: Path, secondWavFile: Path): Unit =
    for {
      firstPcmShorts <- Ffmpeg.shortsFromWav(firstWavFile, LeftChannel)
      secondPcmShorts <- Ffmpeg.shortsFromWav(secondWavFile, LeftChannel)
    } {
      val maxOffset = ThirtySecondsInSamples

      println(s"1st file length (samples): ${firstPcmShorts.length}")
      println(s"2nd file length (samples): ${secondPcmShorts.length}")

      println("Algorithm 1")
      val (minOffset1, minAverage1) = time(
        bestAlignmentSlow(firstPcmShorts, secondPcmShorts, maxOffset))
      println(s"(minOffset, minAverage) = ($minOffset1, $minAverage1)")

      for (zoneCount <- 2 to 10) {
        println(s"Algorithm 2 (zoneCount = $zoneCount)")
        val (minOffset2, minAverage2) = time(
          bestAlignment(firstPcmShorts, secondPcmShorts, maxOffset, zoneCount))
        println(s"(minOffset, minAverage) = ($minOffset2, $minAverage2)")
      }

      println("Algorithm 1 - 2nd execution")
      val (minOffset4, minAverage4) = time(
        bestAlignmentSlow(firstPcmShorts, secondPcmShorts, maxOffset))
      println(s"(minOffset, minAverage) = ($minOffset4, $minAverage4)")

      for (zoneCount <- 2 to 10) {
        println(s"Algorithm 2 - 2nd execution (zoneCount = $zoneCount)")
        val (minOffset5, minAverage5) = time(
          bestAlignment(firstPcmShorts, secondPcmShorts, maxOffset, zoneCount))
        println(s"(minOffset, minAverage) = ($minOffset5, $minAverage5)")
      }
    }
}
