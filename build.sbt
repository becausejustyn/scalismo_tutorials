ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "scalismo_tutorials",
    organization  := "ch.unibas.cs.gravis",
    resolvers += Resolver.bintrayRepo("unibas-gravis", "maven"),
    libraryDependencies ++= Seq("ch.unibas.cs.gravis" %% "scalismo-ui" % "0.91.2")
  )
