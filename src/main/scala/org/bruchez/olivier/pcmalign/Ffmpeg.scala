package org.bruchez.olivier.pcmalign

import java.io.File
import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Path}
import scala.sys.process._
import scala.util._

sealed trait StereoChannel { def channelNumber: Int }
case object LeftChannel extends StereoChannel { override val channelNumber = 0 }
case object RightChannel extends StereoChannel { override val channelNumber = 1 }

object Ffmpeg {
  def monoS16lePcmFromWavFile(wavFile: Path, stereoChannel: StereoChannel): Try[Path] = {
    val outputFile = File.createTempFile("pcm-align-", ".pcm")

    val cmd = Seq(
      "ffmpeg",
      "-y",
      "-i",
      wavFile.toString,
      "-map_channel",
      s"0.0.${stereoChannel.channelNumber}",
      "-f",
      "s16le",
      "-acodec",
      "pcm_s16le",
      outputFile.getAbsolutePath
    )

    val nullProcessLogger = new ProcessLogger {
      override def out(s: => String): Unit = ()
      override def err(s: => String): Unit = ()
      override def buffer[T](f: => T): T = f
    }

    Try(cmd.!!(nullProcessLogger)) match {
      case Success(_) =>
        Success(outputFile.toPath)
      case Failure(throwable) =>
        outputFile.delete()
        System.err.println(s"Following command failed: $cmd")
        Failure(throwable)
    }
  }

  def shortsFromWav(wavFile: Path, stereoChannel: StereoChannel): Try[Array[Short]] =
    Ffmpeg.monoS16lePcmFromWavFile(wavFile, stereoChannel) map { pcmFile =>
      try {
        val buffer = Files.readAllBytes(pcmFile)

        val byteBuffer = ByteBuffer.wrap(buffer)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val shorts = new Array[Short](buffer.length / 2)
        byteBuffer.asShortBuffer.get(shorts)

        shorts
      } finally {
        pcmFile.toFile.delete()
      }
    }
}
