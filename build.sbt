import sbt.Keys.resolvers

lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.6.0-M1"
lazy val monixVersion   = "3.0.0-RC2"
lazy val airframeLogVersion = "0.50"
lazy val scalaTestVersion = "3.0.5"
lazy val opcClientVersion = "0.3.0-SNAPSHOT"
lazy val mqttClientVersion = "1.0.2"
lazy val scalaAsyncVersion = "0.10.0"
lazy val scalaJavaCompactVersion = "0.9.0"
lazy val monixKafkaClientVersion = "1.0.0-RC3"
lazy val jodaTimeVersion = "2.10.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots",
      "MQTT Repository"     at "https://repo.eclipse.org/content/repositories/paho-releases/"
    ),
    name := "opc-client-stream",
    libraryDependencies ++= Seq(
      "io.monix"           %% "monix"                % monixVersion,
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,
      "joda-time"           % "joda-time"            % jodaTimeVersion,

      // For all connectors / sources / sinks
      "org.eclipse.milo"   %  "sdk-client"               % opcClientVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % mqttClientVersion,
      "io.monix"           %% "monix-kafka-1x"           % monixKafkaClientVersion,

      // For dealing with logging
      "org.wvlet.airframe" %% "airframe-log"         % airframeLogVersion,

      // For dealing with Java futures and scala future handling
      "org.scala-lang.modules" %% "scala-java8-compat" % scalaJavaCompactVersion,
      "org.scala-lang.modules" %% "scala-async"        % scalaAsyncVersion,
      
      // For dealing with linear optimizer (TODO: Remove this after experimentation)
      "org.scalanlp" %% "breeze" % "0.13.2",
      "com.github.vagmcs" %% "optimus" % "2.1.0",
      "com.github.vagmcs" %% "optimus-solver-oj" % "2.1.0",
      "com.datumbox" % "lpsolve" % "5.5.2.0",

      "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpVersion  % Test,
      "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion      % Test,
      "com.typesafe.akka"  %% "akka-stream-testkit"  % akkaVersion      % Test,
      "org.scalatest"      %% "scalatest"            % scalaTestVersion % Test
    )
  )
