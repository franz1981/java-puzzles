# Java Benchmark puzzles

## SimpleInliningTest

This test shows the performance impact of 2 different (logging) code patterns.

`SimpleInliningTest::wrongLong` is supposed to be too big (bytecode wise) to be inlined; it can be verified using:
- the compilation log by running the application with `-XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining`
- [JITWatch](https://github.com/AdoptOpenJDK/jitwatch) by copying the source *as it is* into the SandBox and playing with it

*notes*:
it uses an ancient and unsafe technique to avoid dead code removal, JMH would be better, but JitWatch's Sandbox is the main consumer of this.

## ProfilerFallacyExample

The profilers that worths to be tried are: 
1) [JVisualVM CPU Sample Profiler](https://docs.oracle.com/javase/6/docs/technotes/tools/share/jvisualvm.html): `JvmtiEnv::GetAllStackTraces` based 
2) [perf-map-agent](https://github.com/jvm-profiling-tools/perf-map-agent): "mostly" [perf](https://perf.wiki.kernel.org/index.php/Main_Page) based
3) [async-profiler](https://github.com/jvm-profiling-tools/async-profiler): `AsyncGetCallTrace` + [perf](https://perf.wiki.kernel.org/index.php/Main_Page) hybrid

The instruction for the `perf-map-agent` case are packed directly in the javadoc of the test.

In summary:
1) `JVisualVM` could just samples Java stack while on [safepoint](http://blog.ragozin.info/2012/10/safepoints-in-hotspot-jvm.html)
2) `perf-map-agent` uses `perf`, can profile any stack (native + Java), but need to force the JVM to preserve the frame pointer and could read broken frames
3) `async-profiler` uses `perf` for native (JVM/Kernel) calls while `AsyncGetCallTrace` for Java ones (`JFR`, `Solaris Studio` do the same)
trying to match between the two worlds (could read broken frames on both sides)

*notes*:
Please remember to kill it or it will kill your CPU!!!!Hot stuff

## ArrayfillBenchmark

It is mostly based on the awesome [Nitsan Wakart article](http://psy-lob-saw.blogspot.it/2015/04/on-arraysfill-intrinsics-superword-and.html) and it helps 
to understand how most legends around `byte[]` common operations are false and need proper (and correct) measurements. 

## MonomorphicCallSiteBenchmark

It is an attempt to show the best case scenarios (monomorphic call site) with several code patterns:
- `getDefaultId`: `default` method call with single interface/implementor
- `getInterfaceId`: call of a method declared on an interface
- `getAbstractId`: call of an abstract method declared on an abstract class, but implemented on a concrete one
- `getId`: call of a (final) method declared and implemented on an abstract class

A better and much deeper explanation could be found here: https://shipilev.net/blog/2015/black-magic-method-dispatch/#_monomorphic_cases

 


 
  