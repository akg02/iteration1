name := """gitterific"""
organization := "ca.concordia"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.mockito" % "mockito-core" % "2.10.0" % "test",
  ehcache
)