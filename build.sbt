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

val Scala213 = "2.13.18"
val Scala212 = "2.12.21"
val Scala3 = "3.3.9"
ThisBuild / crossScalaVersions := Seq(Scala213, Scala212, Scala3)
ThisBuild / githubWorkflowScalaVersions := Seq("2.13", "2.12", "3")
ThisBuild / scalaVersion := Scala213
ThisBuild / tlJdkRelease := Option(8)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

// Maven Central rate-limits downloads by network egress, and GitHub-hosted runners share theirs, so CI
// keeps its Central traffic down (https://central.sonatype.org/faq/429-error/).
//
// Run CI for pull requests into main and for pushes to main and release tags. A push to a pull request's
// branch is covered by its pull_request run, so each change builds once.
ThisBuild / githubWorkflowTargetBranches := Seq("main")
// setup-java's sbt cache restores only an exact match on the build files' hash. This cache falls back to the
// newest one with the same prefix (normally main's), so after a change to the build files `sbt update` fetches
// only what changed.
//
// Only pushes to main save it: a pull request's cache is visible to that pull request alone, and each is about
// as large as main's, so saving them would fill the repository's cache quota and evict main's.
val sbtDependencyCachePaths: String = List("~/.cache/coursier", "~/.ivy2/cache", "~/.sbt").mkString("\n")
val sbtDependencyCacheKeyPrefix: String = "${{ runner.os }}-sbt-dependencies-"
def sbtDependencyCacheKey(dependencyPatterns: Seq[String]): String = {
  val dependencyFiles = dependencyPatterns.map(pattern => s"'$pattern'").mkString(", ")
  s"$sbtDependencyCacheKeyPrefix$${{ hashFiles($dependencyFiles) }}"
}
ThisBuild / githubWorkflowGeneratedCacheSteps := Seq(
  WorkflowStep.Use(
    UseRef.Public("actions", "cache/restore", "v6"),
    name = Some("Restore sbt dependencies"),
    id = Some("sbt-dependency-cache"),
    params = Map(
      "path" -> sbtDependencyCachePaths,
      "key" -> sbtDependencyCacheKey(githubWorkflowDependencyPatterns.value),
      "restore-keys" -> sbtDependencyCacheKeyPrefix,
    ),
  ),
  // MiMa resolves the previous release's artifacts itself, outside `update`, so they're fetched here too and
  // land in the cache with everything else.
  WorkflowStep.Sbt(
    List("+update", "+mimaPreviousClassfiles"),
    name = Some("sbt update"),
    cond = Some("steps.sbt-dependency-cache.outputs.cache-hit != 'true'"),
    preamble = false,
  ),
)
ThisBuild / githubWorkflowBuildPostamble += WorkflowStep.Use(
  UseRef.Public("actions", "cache/save", "v6"),
  name = Some("Save sbt dependencies"),
  cond = Some(
    "github.event_name == 'push' && github.ref == 'refs/heads/main' && steps.sbt-dependency-cache.outputs.cache-hit != 'true'"
  ),
  params = Map(
    "path" -> sbtDependencyCachePaths,
    "key" -> sbtDependencyCacheKey(githubWorkflowDependencyPatterns.value),
  ),
)
ThisBuild / githubWorkflowJobSetup := List(WorkflowStep.CheckoutFull, WorkflowStep.SetupSbt) ++
  WorkflowStep.SetupJava(githubWorkflowJavaVersions.value.toList, enableCaching = false) ++
  githubWorkflowGeneratedCacheSteps.value
ThisBuild / tlCiReleaseBranches += "main"

lazy val `epimetheus-server-root` =
  project.in(file("."))
    .settings(
      publish / skip := true,
      publishArtifact := false,
    )
    .aggregate(core)

lazy val core = project.in(file("core"))
  .settings(
    name := "monitoring-server",
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.39",
      "org.http4s" %%% "http4s-dsl" % "0.23.39",
      "io.chrisdavenport" %%% "epimetheus" % "0.5.0",
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0" % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % "2.0.0-M2" % Test,
      "com.eed3si9n.expecty" %%% "expecty" % "0.16.0" % Test,
      "ch.qos.logback" % "logback-classic" % "1.4.14" % Test,
    )
  )
