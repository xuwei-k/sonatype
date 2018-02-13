addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.2")
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.4.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Yno-adapted-args"
)
