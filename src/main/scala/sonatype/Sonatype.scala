package sonatype

import sbt.io.{IO, Path}
import sbt.io.syntax._

object Sonatype {

  def main(args: Array[String]): Unit = {
    run(args.toList)
  }

  def run(args: List[String]): Int = {
    args match {
      case Nil | (_ :: Nil) =>
        Console.err.println("invalid arguments\nusage <profileName> <commands>")
        -1
      case profileName :: commands =>
        run0(profileName, commands)
    }
  }

  def run0(profileName: String, commands: List[String]) = {
    val sbtVersion = "1.3.0"
    val launcher = Path.userHome / ".sbt/launchers" / sbtVersion / "sbt-launch.jar"
    if (!launcher.isFile) {
      val launcherURL = url(s"https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/${sbtVersion}/sbt-launch.jar")
      sbt.io.Using.urlInputStream(launcherURL) { inputStream =>
        IO.transfer(inputStream, launcher)
      }
    }
    IO.withTemporaryDirectory { dir =>
      IO.write(dir / "project/build.properties", s"sbt.version=${sbtVersion}")
      IO.write(dir / "project/plugin.sbt", """addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.7")""")
      IO.write(dir / "build.sbt", s"""sonatypeProfileName := "${profileName}"""")

      sys.process
        .Process(
          command = "java" :: "-Xmx2G" :: "-jar" :: launcher.getCanonicalPath :: commands,
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
