// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "7.1" // your current series x.y

ThisBuild / organization := "com.dwolla"
ThisBuild / organizationName := "Dwolla"
ThisBuild / homepage := Some(url("https://github.com/Dwolla/epimetheus-server"))
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
ThisBuild / githubWorkflowScalaVersions := Seq("2.13", "2.12", "3")
ThisBuild / scalaVersion := Scala213
ThisBuild / tlJdkRelease := Option(8)

lazy val `epimetheus-server-root` = project.in(file(".")).aggregate(core)

lazy val core = project.in(file("core"))
  .settings(
    name := "monitoring-server",
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.16",
      "org.http4s" %%% "http4s-dsl" % "0.23.16",
      "io.chrisdavenport" %%% "epimetheus" % "0.5.0-M2",
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
      "com.eed3si9n.expecty" %%% "expecty" % "0.16.0" % Test,
      "ch.qos.logback" % "logback-classic" % "1.3.4" % Test,
    )
  )
