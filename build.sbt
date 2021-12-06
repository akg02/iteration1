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

  "com.typesafe.akka" %% "akka-actor" % "2.6.14",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.14" % Test,

  // For testing web socket
  "org.awaitility" % "awaitility" % "4.0.1" % Test,
)

// Increase Websocket timeout
PlayKeys.devSettings += "play.server.http.idleTimeout" -> "3600s"

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes 

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
