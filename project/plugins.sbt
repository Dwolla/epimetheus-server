addSbtPlugin("org.typelevel" % "sbt-typelevel-ci-release" % "0.4.16-8-c4e104f-SNAPSHOT")
addSbtPlugin("org.typelevel" % "sbt-typelevel-settings" % "0.4.16-8-c4e104f-SNAPSHOT")
addSbtPlugin("org.typelevel" % "sbt-typelevel-mergify" % "0.4.16-8-c4e104f-SNAPSHOT")

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
