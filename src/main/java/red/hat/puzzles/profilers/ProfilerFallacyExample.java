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

package red.hat.puzzles.profilers;

import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ProfilerFallacyExample {

    private static int WORKS;

    /**
     * JVisualVM:
     * <p>
     * Run with -XX:+PrintGCApplicationStoppedTime to see all TTSP added by JVisualVM.
     * <p>
     * Start the tool with:
     * $ jvisualvm
     * <p>
     * Notes:
     * <p>
     * -self time           counts the total time spent in that method own code (no other method calls),
     *                      including the amount of time spent on locks or other blocking behaviour
     * -self time (cpu)     counts the total time spent in that method own code (no other method calls),
     *                      excluding the amount of time the thread was blocked
     * -total time          counts the total time spent in that method (with other method calls),
     *                      including the amount of time spent on locks or other blocking behaviour
     * -total time (cpu)    counts the total time spent in that method (with other method calls),
     *                      excluding the amount of time spent on locks or other blocking behaviour
     * <p>
     * perf-map-agent instructions:
     * <p>
     * Run me with: -XX:+PreserveFramePointer -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
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

    private static byte[] BYTES;

    public static void main(String[] args) throws IOException {
        //tuning the work to be done on countedLoop will force the safepoint poll after it
        //to be delayed, making all the other threads to wait to reach the global safepoint:
        //you can see on the GC logs that "Stopping threads took: *** seconds" is directly proportional to it.
        int timeToSafepoint = 1000;
        if (args.length > 0) {
            timeToSafepoint = Integer.parseInt(args[0]);
        }
        System.out.println("timeToSafepoint tokens = " + timeToSafepoint);
        WORKS = timeToSafepoint;
        BYTES = new byte[1024 * 1024];
        //warmup and compilation of methods
        for (int i = 0; i < 100000; i++) {
            blameJustUncounted(1);
        }
        for (int i = 0; i < 100000; i++) {
            doIntrinsicAndBlameTheCallerOfThePollExit();
        }
        for (int i = 0; i < 100000; i++) {
            doCountedAndBlameTheCallerOfThePollExit(1);
        }
        //end warmup
        final ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(ProfilerFallacyExample::blameJustUncountedLoop);
        executorService.submit(ProfilerFallacyExample::blameTheWrongMethodInsteadOfNativeLoop);
        executorService.submit(ProfilerFallacyExample::blameTheWrongMethodInsteadOfTheCountedLoop);
        executorService.submit(ProfilerFallacyExample::sleepOneSecondLoop);
        System.out.println("Press any key to stop, but wait OSR to happen before start profiling with perf...");
        System.in.read();
        executorService.shutdownNow();
        System.out.println(BYTES);
    }

    /**
     * It should be visible on JVisualVM but not on hybrid/native profilers
     */
    private static void sleepOneSecondLoop() {
        final long oneSecInNanos = TimeUnit.SECONDS.toNanos(1);
        while (!Thread.currentThread().isInterrupted()) {
            //sometime it could fails before having reached the deadline
            LockSupport.parkNanos(oneSecInNanos);
        }
    }

    private static void blameJustUncountedLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            blameJustUncounted(WORKS);
        }
    }

    private static void blameJustUncounted(long works) {
        countedLoop((int)works);
        //consumeCPU will be inlined and it will be blamed
        //for everything because it contains the last poll on it
        Blackhole.consumeCPU(works);
    }

    private static final boolean ENABLE_LOGGING = Boolean.getBoolean("safiansgfdugan");

    private static void handWrittenNotInlineableCall() {
        if (ENABLE_LOGGING) {
            final long b = System.nanoTime();
            //long logic to create the String used for logging
            final StringBuilder builder = new StringBuilder();
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            builder.append(b);
            System.out.println(builder);
        }
    }

    private static void blameTheWrongMethodInsteadOfNativeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            doIntrinsicAndBlameTheCallerOfThePollExit();
        }
    }

    private static void doIntrinsicAndBlameTheCallerOfThePollExit() {
        //http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/87ee5ee27509/src/share/vm/classfile/vmSymbols.hpp#l708
        System.arraycopy(BYTES, 0, BYTES, 0, BYTES.length);
        //no poll or exit poll on intrinsic
        blameMePlease();
    }

    private static void blameTheWrongMethodInsteadOfTheCountedLoop() {
        long k = 0;
        while (!Thread.currentThread().isInterrupted()) {
            doCountedAndBlameTheCallerOfThePollExit(100);
        }
        System.out.println(k);
    }

    private static void doCountedAndBlameTheCallerOfThePollExit(int limit){
        countedLoop(limit);
        //no poll or exit poll on countedLoop
        blameMePlease();
    }

    private static void blameMePlease() {
        handWrittenNotInlineableCall();
    }

    private static long countedLoop(int limit) {
        long k = 0;
        for (int l = 0; l < limit; l++) {
            for (int i = 0; i < limit; i++) {
                for (int j = 0; j < 2; j++) {
                    k++;
                    if ((k % 2) == 1)
                        k += l;
                }
            }

        }
        return k;
    }
}
