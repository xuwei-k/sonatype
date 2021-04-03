name := "sonatype"

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](sbtVersion)
buildInfoPackage := "sonatype"
buildInfoObject := "SonatypeBuildInfo"

Global / onChangedBuildSource := ReloadOnSourceChanges

publishTo := sonatypePublishToBundle.value

scalaVersion := "2.12.11"
organization := "com.github.xuwei-k"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/mit-license"))
homepage := Some(url("https://github.com/xuwei-k/sonatype"))

libraryDependencies += "org.scala-sbt" %% "io" % "1.5.0"

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
    csRun.toTask(" sonatype com.github.xuwei-k sonatypeRepository"),
    Def.task {
      sys.process.Process(s"git checkout ${launchconfigFile}").!
    }
  )
  .value

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

(Compile / doc / scalacOptions) ++= {
  val tag = tagOrHash.value
  Seq(
    "-sourcepath",
    (LocalRootProject / baseDirectory).value.getAbsolutePath,
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
  releaseStepCommandAndRemaining("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

updateLaunchconfig := updateLaunchconfigTask(true).value

def updateLaunchconfigTask(commit: Boolean) =
  Def.task {
    val mainClassName = (Compile / discoveredMainClasses).value match {
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
    |  maven-central
    |""".stripMargin
    IO.write(launchconfigFile, launchconfig)
    val s = streams.value.log
    if (commit) {
      val git = new sbtrelease.Git((LocalRootProject / baseDirectory).value)
      git.add(launchconfigFile.getCanonicalPath) ! s
      git.commit(message = "update launchconfig", sign = false, signOff = false) ! s
    }
    launchconfigFile
  }
