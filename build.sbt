name := "pcm-align"
version := "1.0"
scalaVersion := "2.12.8"

libraryDependencies += "commons-io" % "commons-io" % "2.6"

mainClass in assembly := Some("org.bruchez.olivier.pcmalign.PcmAlign")

assemblyJarName in assembly := "pcm-align.jar"

scalafmtOnCompile in ThisBuild := true
