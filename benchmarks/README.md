# Benchmarks
Holds any benchmarks relevant to the clients or core.  


Either build and run through IntelliJ, or:

Build with `gradle shadowJar`

I build with gradle 5.1 and Java 8.

Run with `java -jar build/libs/benchmarks-1.0-SNAPSHOT.jar`

Change `Bench.ForkedTime` to `Bench.LocalTime` to get a less accurate but much faster result, during local development.


## Why not JMH?
JMH on gradle relies on a plugin.  Hit a lot of random compilation issues that seemed to involve the gradle cache and haven't been fixed despite
being open for years:

https://github.com/melix/jmh-gradle-plugin/issues/89
https://github.com/melix/jmh-gradle-plugin/issues/90

None of the workarounds worked, so trying ScalaMeter instead.