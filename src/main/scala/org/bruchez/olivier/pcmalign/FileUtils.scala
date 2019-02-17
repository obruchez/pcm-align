package org.bruchez.olivier.pcmalign

import java.nio.file._
import org.apache.commons.io.FilenameUtils
import scala.collection.JavaConverters._

object FileUtils {
  def allFilesInPath(path: Path, recursive: Boolean): Seq[Path] =
    if (recursive) {
      Files.walk(path, FileVisitOption.FOLLOW_LINKS).iterator().asScala.toSeq
    } else {
      Files.newDirectoryStream(path).iterator().asScala.toSeq
    }

  def baseNameAndExtension(path: Path): (String, Option[String]) =
    (FilenameUtils.getBaseName(path.toString),
     Some(FilenameUtils.getExtension(path.toString)).filterNot(_.isEmpty))
}
