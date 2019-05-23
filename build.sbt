import sbt.Keys.resolvers

lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.6.0-M1"
lazy val monixVersion   = "3.0.0-RC2"
lazy val airframeLogVersion = "0.50"
lazy val scalaTestVersion = "3.0.5"
lazy val opcClientVersion = "0.3.0-SNAPSHOT"
lazy val scalaAsyncVersion = "0.10.0"
lazy val scalaJavaCompactVersion = "0.9.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots",
    ),
    name := "opc-client-stream",
    libraryDependencies ++= Seq(
      "io.monix"           %% "monix"                % monixVersion,
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,
      "org.eclipse.milo"   %  "sdk-client"           % opcClientVersion,

      // For dealing with logging
      "org.wvlet.airframe" %% "airframe-log"         % airframeLogVersion,

      // For dealing with Java futures and scala future handling
      "org.scala-lang.modules" %% "scala-java8-compat" % scalaJavaCompactVersion,
      "org.scala-lang.modules" %% "scala-async"        % scalaAsyncVersion,

      "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpVersion  % Test,
      "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion      % Test,
      "com.typesafe.akka"  %% "akka-stream-testkit"  % akkaVersion      % Test,
      "org.scalatest"      %% "scalatest"            % scalaTestVersion % Test
    )
  )
