organization := "roomframework"

name := "roomframework"

version := "0.9.6"

scalaVersion := "2.11.6"

scalaSource in Compile := baseDirectory.value / "src"	

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "[2.3.0,)" % "provided",
  "com.typesafe.play" %% "play-cache" % "[2.3.0,)" % "provided",
  "net.debasishg" %% "redisclient" % "[2.15,)"
)

publishTo := Some(Resolver.file("givery repo",file("../sbt-repo"))(Patterns(true, Resolver.mavenStyleBasePattern)))

scalacOptions += "-feature"

scalacOptions += "-deprecation"