name := "pcm-align"
version := "0.1"
scalaVersion := "2.12.7"

libraryDependencies += "commons-io" % "commons-io" % "2.6"
//libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"

mainClass in assembly := Some("org.bruchez.olivier.pcmalign.PcmAlign")

assemblyJarName in assembly := "pcm-align.jar"

scalafmtOnCompile in ThisBuild := true

assemblyMergeStrategy in assembly := {
  /*
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last

  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last*/
  case PathList("javax", "inject", xs @ _*)    => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*)      => MergeStrategy.last
  case "git.properties"                        => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
