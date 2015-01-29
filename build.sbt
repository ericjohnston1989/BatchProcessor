name := "batch_processor"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "2.4.15" % "test",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5"
)

testOptions += Tests.Setup( cl =>
   cl.loadClass("org.slf4j.LoggerFactory").
     getMethod("getLogger",cl.loadClass("java.lang.String")).
     invoke(null,"ROOT")
)