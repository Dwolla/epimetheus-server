// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "com.dwolla"
ThisBuild / organizationName := "Dwolla"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("bpholt", "Brian Holt")
)
ThisBuild / tlSonatypeUseLegacyHost := false

val Scala213 = "2.13.10"
val Scala212 = "2.12.17"
val Scala3 = "3.2.0"
ThisBuild / crossScalaVersions := Seq(Scala213, Scala212, Scala3)
ThisBuild / scalaVersion := Scala213

lazy val `epimetheus-server-root` = project.in(file(".")).aggregate(core)

lazy val core = project.in(file("core"))
  .settings(
    name := "epimetheus-server",
    libraryDependencies ++= Seq(
    )
  )
