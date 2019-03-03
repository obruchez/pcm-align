package org.bruchez.olivier.pcmalign

import scala.collection.mutable

case class Distance(absoluteDifferenceCounts: Map[Int, Int]) {
  lazy val totalCount: Int = absoluteDifferenceCounts.values.sum

  lazy val averageOfAbsoluteDifferences: Double =
    absoluteDifferenceCounts.map(kv => kv._1.toLong * kv._2).sum.toDouble / totalCount

  def percentile(percent: Double): Int = {
    // See https://en.wikipedia.org/wiki/Percentile
    val cumulatedDifferenceCounts =
      absoluteDifferenceCounts.toSeq.sortBy(_._1).foldLeft(List[(Int, Int)]()) {
        case (cumulatedDifferenceCountsAcc, (currentValue, currentCount)) =>
          val previousCumulatedCount =
            cumulatedDifferenceCountsAcc.headOption.map(_._2).getOrElse(0)

          val currentCumulatedCount = previousCumulatedCount + currentCount

          (currentValue -> currentCumulatedCount) :: cumulatedDifferenceCountsAcc
      }

    cumulatedDifferenceCounts
      .sortBy(_._1)
      .map(kv => kv._1 -> kv._2 * 100.0 / totalCount)
      .find(_._2 >= percent)
      .get
      ._1
  }

  lazy val aligned: Boolean = percentile(Distance.ReferencePercentile) <= Distance.ThresholdValue

  lazy val asString: String =
    f"avg diff = $averageOfAbsoluteDifferences%.3f, " +
      s"percentile(${Distance.ReferencePercentile}) = ${percentile(Distance.ReferencePercentile)}, " +
      s"aligned = ${if (aligned) "true" else "false"}"
}

object Distance {
  val ReferencePercentile = 99.99
  val ThresholdValue = 50

  def apply(firstArray: Array[Short],
            secondArray: Array[Short],
            offsetInFirstArray: Int): Distance = {
    val (firstArrayToProcess, secondArrayToProcess, positiveOffsetInFirstArray) =
      if (offsetInFirstArray >= 0) {
        (firstArray, secondArray, offsetInFirstArray)
      } else {
        (secondArray, firstArray, -offsetInFirstArray)
      }

    var firstIndex = positiveOffsetInFirstArray
    var secondIndex = 0

    val absoluteDifferenceCounts = mutable.Map[Int, Int]()

    while (firstIndex < firstArrayToProcess.length && secondIndex < secondArrayToProcess.length) {
      val absoluteDifference =
        math.abs(firstArrayToProcess(firstIndex) - secondArrayToProcess(secondIndex))

      val differenceCount = absoluteDifferenceCounts.getOrElse(absoluteDifference, 0) + 1
      absoluteDifferenceCounts.update(absoluteDifference, differenceCount)

      firstIndex += 1
      secondIndex += 1
    }

    Distance(absoluteDifferenceCounts = absoluteDifferenceCounts.toMap)
  }

  def merged(firstDistance: Distance, secondDistance: Distance): Distance = {
    val absoluteDifferences = firstDistance.absoluteDifferenceCounts.keySet ++ secondDistance.absoluteDifferenceCounts.keySet

    val absoluteDifferenceCounts = (for (absoluteDifference <- absoluteDifferences) yield {
      val firstCount = firstDistance.absoluteDifferenceCounts.getOrElse(absoluteDifference, 0)
      val secondCount = secondDistance.absoluteDifferenceCounts.getOrElse(absoluteDifference, 0)

      absoluteDifference -> (firstCount + secondCount)
    }).toMap

    Distance(absoluteDifferenceCounts)
  }
}
