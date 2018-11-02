name := "sonatype"

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

scalaVersion := "2.12.7"
organization := "com.github.xuwei-k"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/mit-license"))
homepage := Some(url("https://github.com/xuwei-k/sonatype"))

libraryDependencies += "org.scala-sbt" %% "io" % "1.2.2"

pomExtra :=
  <scm>
    <url>git@github.com:xuwei-k/sonatype.git</url>
    <connection>scm:git:git@github.com:xuwei-k/sonatype.git</connection>
  </scm>
  <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>

enablePlugins(ConscriptPlugin)

val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")
val launchconfigFile = file("src/main/conscript/sonatype/launchconfig")

val testConscript = TaskKey[Int]("testConscript")

testConscript := Def
  .sequential(
    updateLaunchconfigTask(false),
    csRun.toTask(" sonatype com.github.xuwei-k sonatypeList"),
    Def.task {
      sys.process.Process(s"git checkout ${launchconfigFile}").!
    }
  )
  .value

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

scalacOptions in (Compile, doc) ++= {
  val tag = tagOrHash.value
  Seq(
    "-sourcepath",
    (baseDirectory in LocalRootProject).value.getAbsolutePath,
    "-doc-source-url",
    s"https://github.com/xuwei-k/sonatype/tree/${tag}€{FILE_PATH}.scala"
  )
}

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xlint",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Yno-adapted-args",
  "-Ywarn-unused"
)

releaseTagName := tagName.value
resolvers += Opts.resolver.sonatypeReleases

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining(";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck"),
  releaseStepTask(testConscript),
  runTest,
  setReleaseVersion,
  releaseStepTask(updateLaunchconfig),
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommandAndRemaining("sonatypeReleaseAll"),
  pushChanges
)

updateLaunchconfig := updateLaunchconfigTask(true).value

def updateLaunchconfigTask(commit: Boolean) = Def.task {
  val mainClassName = (discoveredMainClasses in Compile).value match {
    case Seq(m) => m
    case zeroOrMulti => sys.error(s"could not found main class. $zeroOrMulti")
  }
  val launchconfig = s"""[app]
    |  version: ${version.value}
    |  org: ${organization.value}
    |  name: ${normalizedName.value}
    |  class: ${mainClassName}
    |[scala]
    |  version: ${scalaVersion.value}
    |[repositories]
    |  local
    |  sonatype-releases: https://oss.sonatype.org/content/repositories/releases
    |  maven-central
    |""".stripMargin
  IO.write(launchconfigFile, launchconfig)
  val s = streams.value.log
  if (commit) {
    val git = new sbtrelease.Git((baseDirectory in LocalRootProject).value)
    git.add(launchconfigFile.getCanonicalPath) ! s
    git.commit(message = "update launchconfig", sign = false) ! s
  }
  launchconfigFile
}
