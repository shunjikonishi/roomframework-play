organization := "roomframework"

name := "roomframework"

version := "0.9.5"

scalaVersion := "2.10.4"

scalaSource in Compile := baseDirectory.value / "src"	

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "[2.3.0,)" % "provided",
  "com.typesafe.play" %% "play-cache" % "[2.3.0,)" % "provided",
  "net.debasishg" %% "redisclient" % "[2.13,)"
)

publishTo := Some(Resolver.file("flect repo",file("../maven-repo"))(Patterns(true, Resolver.mavenStyleBasePattern)))

scalacOptions += "-feature"