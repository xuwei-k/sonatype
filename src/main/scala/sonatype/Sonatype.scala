package sonatype

import sbt.io.{IO, Path}
import sbt.io.syntax._

object Sonatype {

  def main(args: Array[String]): Unit = {
    run(args.toList)
  }

  def run(args: List[String]): Int = {
    SonatypeParser.parse(args, Config()) match {
      case None =>
        -1
      case Some(c) =>
        run0(c)
    }
  }

  def run0(config: Config): Int = {
    val sbtVersion = config.sbtVersion
    val launcher = Path.userHome / ".sbt/launchers" / sbtVersion / "sbt-launch.jar"
    if (!launcher.isFile) {
      val launcherURL = if (sbtVersion.startsWith("0.13")) {
        s"https://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/${sbtVersion}/sbt-launch.jar"
      } else {
        s"https://oss.sonatype.org/content/repositories/releases/org/scala-sbt/sbt-launch/${sbtVersion}/sbt-launch.jar"
      }
      sbt.io.Using.urlInputStream(url(launcherURL)) { inputStream =>
        IO.transfer(inputStream, launcher)
      }
    }
    IO.withTemporaryDirectory { dir =>
      IO.write(dir / "project/build.properties", s"sbt.version=${sbtVersion}")
      IO.write(
        dir / "project/plugin.sbt",
        s"""addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "${config.sonatypeVersion}")""")
      IO.write(dir / "build.sbt", s"""sonatypeProfileName := "${config.profileName}"""")

      sys.process
        .Process(
          command = "java" :: config.jvmArgs.toList ::: "-jar" :: launcher.getCanonicalPath :: config.commands.toList,
          cwd = dir
        )
        .!
    }
  }
}

class Sonatype extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) =
    Exit(Sonatype.run(config.arguments.toList))
}
