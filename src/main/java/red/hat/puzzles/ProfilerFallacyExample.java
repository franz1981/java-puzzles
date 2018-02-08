/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.hat.puzzles;

import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

public class ProfilerFallacyExample {

    private static final int WORKS = 1000;

    /**
     * JVisualVM:
     * <p>
     * Run with -XX:+PrintGCApplicationStoppedTime to see all the safepoints added by JVisualVM: it will causes
     * accelerate aging of instances with subsequent moving of young instances into the tenured space...FULL GCs are awaiting :(
     * <p>
     * perf-map-agent instructions:
     * <p>
     * Run me with: -XX:+PreserveFramePointer -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
     * <p>
     * $ jvisualvm
     * <p>
     * $ jps
     * Take the process pid and use it with:
     * <p>
     * $ PERF_COLLAPSE_OPTS="--kernel --tid" PERF_RECORD_FREQ=99 PERF_RECORD_SECONDS=10 PERF_MAP_OPTIONS=unfoldall ~/perf-map-agent/bin/perf-java-flames <pid>
     * <p>
     * While if there isn't any interest into per-thread/inline informations:
     * <p>
     * $ ~/perf-map-agent/bin/perf-java-flames <pid>
     * <p>
     * $ firefox <flamegraph file>
     */

    private static final byte[] bytes = new byte[1024 * 1024];
    private static final Object MEDIUM_LOCK = new Object();

    public static void main(String[] args) throws IOException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(ProfilerFallacyExample::smallPark);
        executorService.submit(ProfilerFallacyExample::mediumParkWithLock);
        executorService.submit(ProfilerFallacyExample::mediumParkWithLock);
        executorService.submit(ProfilerFallacyExample::longPark);
        executorService.submit(ProfilerFallacyExample::doSomethingUncounted);
        executorService.submit(ProfilerFallacyExample::doSomethingHeavyNative);
        System.out.println("Press any key to stop, but wait OSR to happen before start profiling with perf...");
        System.in.read();
        executorService.shutdownNow();
        //TBH the JVM can't perform dead code elimination of doSomethingHeavyNative thanks to JLS on array assignments, but..better safe than sorry...
        System.out.println(bytes);
    }

    private static void mediumParkWithLock() {
        Thread.currentThread().setName("mediumParkWithLock");
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (MEDIUM_LOCK) {
                //1 seconds: which one could see it and how? it makes sense?
                //hint: please read https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#RUNNABLE
                //JVM TI profilers will see it as a time consuming method just because waiting for CPU time due to lock contention
                //is considered a RUNNABLE ie active state
                LockSupport.parkNanos(1_000_000_000L);
            }
        }
    }

    private static void longPark() {
        Thread.currentThread().setName("longPark");
        while (!Thread.currentThread().isInterrupted()) {
            //100 seconds: which one could see it and how? it makes sense?
            //hint: please read https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#RUNNABLE
            LockSupport.parkNanos(100_000_000_000L);
        }
    }

    private static void smallPark() {
        Thread.currentThread().setName("smallPark");
        //it is supposed to consume real CPU BUT
        //it is stealth to most JVM TI (JVM Tool Interface) based profiler due to:
        // - sample bias (interval is coordinated with the park call rate)
        // - parks is resolved with a native call (ie a native call stack isn't a Java stack anymore)
        // - safepoint bias (it can take samples only at safepoint: http://blog.ragozin.info/2012/10/safepoints-in-hotspot-jvm.html)
        while (!Thread.currentThread().isInterrupted()) {
            LockSupport.parkNanos(1);
        }
    }

    private static void doSomethingUncounted() {
        Thread.currentThread().setName("doSomethingUncounted");
        //It MUST NOT be completly stealth on JVM TI profilers BUT maybe sample bias could make it less heavy than expected :(
        //NOTE: Blackhole::consumeCPU implementation force the JVM to put a safepoint on each iteration of its main loop
        while (!Thread.currentThread().isInterrupted()) {
            Blackhole.consumeCPU(WORKS);
        }
    }

    private static void doSomethingHeavyNative() {
        Thread.currentThread().setName("doSomethingHeavyNative");
        //AsyncGetCallTrace ones won't see it due to the presence of well hidden long native calls (ArrayFillBenchmark show it).
        //Commons AGCT based profiler (JFR) will fall due to things like this: https://bugs.openjdk.java.net/browse/JDK-8178287 (ie CPU time not computed due to samples loss)

        //JVMTI ones couldn't see it due to safepoint bias on very long native call
        while (!Thread.currentThread().isInterrupted()) {
            Arrays.fill(bytes, (byte) (System.nanoTime()));
        }
    }
}
