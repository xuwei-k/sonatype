package sonatype

case class Config(
  profileName: String = "",
  commands: Seq[String] = Seq("sonatypeReleaseAll"),
  sbtVersion: String = "0.13.17",
  sonatypeVersion: String = "2.3",
  jvmArgs: Seq[String] = Seq("-Xmx=2G"),
)
