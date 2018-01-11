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

public class FlamegraphExample {

    private static final int WORKS = 1000;

    /**
     * Run me with: -XX:+PreserveFramePointer -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
     * <p>
     * $ jvisualvm
     * <p>
     * $ jps
     * <p>
     * $ PERF_COLLAPSE_OPTS="--kernel --tid" PERF_RECORD_FREQ=99 PERF_RECORD_SECONDS=10 PERF_MAP_OPTIONS=unfoldall ~/perf-map-agent/bin/perf-java-flames <pid>
     * <p>
     * or simpler:
     * <p>
     * $ ~/perf-map-agent/bin/perf-java-flames <pid>
     * <p>
     * $ firefox flamegraph*
     */

    private static final byte[] bytes = new byte[1024 * 1024];

    public static void main(String[] args) throws IOException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(FlamegraphExample::smallPark);
        executorService.submit(FlamegraphExample::longPark);
        executorService.submit(FlamegraphExample::doSomething);
        executorService.submit(FlamegraphExample::doSomethingNative);
        System.out.println("Press any key to stop, but wait OSR to happen before start profiling with perf...");
        System.in.read();
        executorService.shutdownNow();
        //can't perform dead code elimination thanks to JLS on array assignments
        System.out.println(bytes);
    }

    private static void longPark() {
        while (!Thread.currentThread().isInterrupted()) {
            //100 seconds: which one could see it and how? it makes sense?
            //https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html#RUNNABLE shows false positive for epoll_wait/blocking syscalls
            LockSupport.parkNanos(100_000_000_000L);
        }
    }

    private static void smallPark() {
        //it consume real CPU!!!!But it is stealth to most JVM TI (JVM Tool Interface) based profiler due to:
        // - sample bias (interval is coordinated with the park call rate)
        // - it goes native too :O
        //In both case there is a safepoint on call return
        while (!Thread.currentThread().isInterrupted()) {
            LockSupport.parkNanos(1);
        }
    }

    private static void doSomething() {
        while (!Thread.currentThread().isInterrupted()) {
            Blackhole.consumeCPU(WORKS);
        }
    }

    private static void doSomethingNative() {
        //- broken on AsyncGetCallTrace ones due native call
        // -> broken stack frames -> loss of samples -> CPU time is not computed properly (https://bugs.openjdk.java.net/browse/JDK-8178287)
        //- broken on JVMTI ones due to safepoint bias (depending on execution mode, safepoints are mostly at uncounted loop back edge or method return/entry)
        while (!Thread.currentThread().isInterrupted()) {
            Arrays.fill(bytes, (byte) (System.nanoTime()));
        }
    }
}
