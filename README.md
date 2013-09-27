Akka-Persistence Playground
===========================

Trying out various Akka Persistence features with regards to event/command sourcing.

In order to see how the event sourced demo survives a JVM crash start ```sbt``` and do the following:

  - type ```run```
  - watch it crash
  - type ```run```

In order to flush the journal you need to do a ```clean```.
