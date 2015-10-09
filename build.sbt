name := "PriceCrypter"

organization := "org.openx.market.ssrtb"

version := "0.0.4-run6"

crossPaths := false
autoScalaLibrary := false

scalacOptions += "-deprecation"

val nexus = "http://maven.rundsp.com:8081/nexus/"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishTo := Some("releases" at nexus + "content/repositories/releases")

resolvers ++= Seq(
  "Local Maven Repository" at "file:///" + Path.userHome + "/.m2/repository"
)

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.5" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "commons-codec" % "commons-codec" % "1.9"
)
