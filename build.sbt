name := "pcm-align"
version := "1.0"
scalaVersion := "2.13.8"

libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"

assembly / mainClass := Some("org.bruchez.olivier.pcmalign.PcmAlign")

assembly / assemblyJarName := "pcm-align.jar"

ThisBuild / scalafmtOnCompile := true
