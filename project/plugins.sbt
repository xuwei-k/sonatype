addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.8")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.3")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Yno-adapted-args"
)
