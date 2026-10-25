ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.9.0"

lazy val root = (project in file("."))
  .settings(
    name := "lab2_kitpo",
    libraryDependencies ++= {
      val scalaFxVersion = "21.0.0-R32"
      val javafxVersion  = "21.0.6"
      val osName = System.getProperty("os.name") match {
        case n if n.toLowerCase.startsWith("win") => "win"
        case n if n.toLowerCase.startsWith("mac") => "mac"
        case _                                    => "linux"
      }
      Seq(
        "org.json"     % "json"           % "20240303",
        "org.scalafx" %% "scalafx"        % scalaFxVersion,
        "org.openjfx"  % "javafx-base"    % javafxVersion % "compile" classifier osName,
        "org.openjfx"  % "javafx-graphics" % javafxVersion % "compile" classifier osName,
        "org.openjfx"  % "javafx-controls" % javafxVersion % "compile" classifier osName
      )
    },
    // Fork JVM for run/runMain — JavaFX requires its own process
    Compile / run / fork := true
  )
