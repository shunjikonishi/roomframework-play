organization := "roomframework"

name := "roomframework"

version := "0.9.4"

scalaVersion := "2.11.1"

scalaSource in Compile := baseDirectory.value / "src"	

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.6",
  "com.typesafe.play" %% "play-cache" % "2.3.6",
  "net.debasishg" %% "redisclient" % "2.13"
)

publishTo := Some(Resolver.file("flect repo",file("../maven-repo"))(Patterns(true, Resolver.mavenStyleBasePattern)))

scalacOptions += "-feature"