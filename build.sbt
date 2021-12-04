name := """gitterific"""
organization := "ca.concordia"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  guice,
  ws,
  // Java11 requires mockito 4.0.0
  "org.mockito" % "mockito-core" % "4.0.0" % "test",
  caffeine,
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.10" % Test
)