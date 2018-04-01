package sonatype

object SonatypeParser extends scopt.OptionParser[Config]("sonatype") {
  arg[String]("<sonatype profile name>").action { (profileName, conf) =>
    conf.copy(profileName = profileName)
  }

  arg[Seq[String]]("commands").action { (commands, conf) =>
    conf.copy(commands = commands)
  }

  opt[String]("sbt-version").action { (v, conf) =>
    conf.copy(sbtVersion = v)
  }

  opt[String]("sonatype-version").action { (v, conf) =>
    conf.copy(sonatypeVersion = v)
  }

  opt[Seq[String]]('j', "jvm-args").action { (a, conf) =>
    conf.copy(jvmArgs = a)
  }
}
