name := "playground"

version := "0.1"

scalaVersion := "2.10.2"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3-20130916-200212",
  "com.typesafe.akka" %% "akka-testkit"                  % "2.3-20130916-200212",
  "org.scalatest" 	  %% "scalatest"                     % "2.0.M6" % "test"
)