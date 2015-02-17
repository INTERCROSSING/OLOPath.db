Nice.scalaProject

//com.github.retronym.SbtOneJar.oneJarSettings
import AssemblyKeys._


name := "olo"

description := "OLOPath.db"

organization := "intercrossing"

bucketSuffix := "era7.com"

//mainClass in oneJar := Some("ohnosequences.gde.CLI")

fatArtifactSettings

libraryDependencies ++= Seq(
  "commons-io"     % "commons-io" % "2.1",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

libraryDependencies += "com.thinkaurelius.titan" % "titan-core" % "0.5.3"

libraryDependencies += "com.thinkaurelius.titan" % "titan-berkeleyje" % "0.5.3"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.0" % "test"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.1.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2"


testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "40", "-minSuccessfulTests", "10", "-workers", "1", "-verbosity", "1")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case "log4j.properties" => MergeStrategy.first
  case "overview.html" => MergeStrategy.first
  case PathList("org", "apache", "commons", _*) => MergeStrategy.first
  case x => old(x)
}
}


