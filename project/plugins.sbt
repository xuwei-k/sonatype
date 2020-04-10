addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.3")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Yno-adapted-args"
)
