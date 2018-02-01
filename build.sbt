name := "Cross-compiled Pluperfect Utilities Library"

crossScalaVersions := Seq("2.11.8", "2.12.3")

scalaVersion := "2.12.3"

lazy val root = project.in(file(".")).
aggregate(crossedJVM, crossedJS).
settings(
  publish := {},
  publishLocal := {}

)

val circeVersion = "0.9.0"

lazy val crossed = crossProject.in(file(".")).
settings(
  name := "pluperfectutils",
  organization := "edu.furman.fufolio",
  version := "1.0.0",
  licenses += ("GPL-3.0",url("https://opensource.org/licenses/gpl-3.0.html")),
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("neelsmith", "maven"),
  libraryDependencies ++= Seq(
    "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
    "org.scalatest" %%% "scalatest" % "3.0.1" % "test",

    "edu.holycross.shot.cite" %% "xcite" % "3.2.2" from "file:///cite/scala/unmanaged_jars/xcite_2.12-3.2.2.jar",
    "edu.holycross.shot.cite" %%% "xcite" % "3.2.2" from "file:///cite/scala/unmanaged_jars/xcite_sjs0.6_2.12-3.2.2.jar",

    "edu.holycross.shot" %% "cex" % "6.1.0",

    "edu.holycross.shot" %% "citerelations" % "2.0.1",

    "edu.holycross.shot" %% "ohco2" % "10.4.1" from "file:///cite/scala/unmanaged_jars/ohco2_2.12-10.4.1.jar",
    "edu.holycross.shot" %%% "ohco2" % "10.4.1" from "file:///cite/scala/unmanaged_jars/ohco2_sjs0.6_2.12-10.4.1.jar",
    "edu.holycross.shot" %% "citebinaryimage" % "1.0.0" from "file:///cite/scala/unmanaged_jars/citebinaryimage_2.12-1.0.0.jar",
    "edu.holycross.shot" %% "scm" % "5.1.10" from "file:///cite/scala/unmanaged_jars/scm_2.12-5.1.10.jar",
    "edu.holycross.shot" %%% "scm" % "5.1.10" from "file:///cite/scala/unmanaged_jars/scm_sjs0.6_2.12-5.1.10.jar",
    "edu.holycross.shot" %% "citeobj" % "5.2.0" from "file:///cite/scala/unmanaged_jars/citeobj_2.12-5.2.0.jar",
    "edu.holycross.shot" %%% "citeobj" % "5.2.0" from "file:///cite/scala/unmanaged_jars/citeobj_sjs0.6_2.12-5.2.0.jar"
  ),
  
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core",
    "io.circe" %%% "circe-generic",
    "io.circe" %%% "circe-optics",
    "io.circe" %%% "circe-parser"
  ).map(_ % circeVersion)
).
jvmSettings(

).
jsSettings(
  skip in packageJSDependencies := false,
  scalaJSUseMainModuleInitializer in Compile := true
)

lazy val crossedJVM = crossed.jvm
lazy val crossedJS = crossed.js.enablePlugins(ScalaJSPlugin)
