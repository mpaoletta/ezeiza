name := "Ezeiza"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies += "com.google.guava" % "guava" % "r09"

libraryDependencies += "org.apache.poi" % "poi" % "3.7"

libraryDependencies += "commons-collections" % "commons-collections" % "3.2.1"

libraryDependencies += "net.sf.jxls" % "jxls-core" % "1.0" withSources()

libraryDependencies += "net.sf.jxls" % "jxls-reader" % "1.0" withSources()

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.7"

libraryDependencies ++= {
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV withSources(),
    "io.spray"            %   "spray-httpx"     % sprayV withSources(),
    "io.spray"            %   "spray-routing" % sprayV withSources(),
    "io.spray"            %   "spray-util" % sprayV withSources(),
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test"
  )
}


libraryDependencies += "io.spray" %%  "spray-json" % "1.2.6" withSources()


seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

mainClass := Some("com.ia.ezeiza.Ezeiza")
