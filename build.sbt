organization := "roomframework"

name := "roomframework"

version := "0.9.2"

scalaSource in Compile := baseDirectory.value / "src"	

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.2.0",
  "net.debasishg" % "redisclient_2.10" % "2.12"
)

publishTo := Some(Resolver.file("flect repo",file("../../../maven-repo"))(Patterns(true, Resolver.mavenStyleBasePattern)))