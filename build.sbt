import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._

name := """jaxjug-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "angularjs" % "1.2.21",
  "org.webjars" % "angular-ui-bootstrap" % "0.11.0-2",
  "org.webjars" % "angular-ui-router" % "0.2.10-1"
)


// Packaging for Docker

dockerBaseImage in Docker := "dockerfile/java"

maintainer in Docker := "Bill Carlson <bcarlson@bridgegateintl.com>"

dockerRepository := Some("coacoas")

dockerExposedPorts in Docker := Seq(9000,9443)
