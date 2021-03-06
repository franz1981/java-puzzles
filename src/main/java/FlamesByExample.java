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

import java.io.IOException;

public class FlamesByExample {

    private static final int WORKS = 1000;

    public static void main(String[] args) throws IOException {
        /**
         * Run me with:
         * -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+PreserveFramePointer
         *
         * perf-map-agent instructions:
         * <p>
         * -XX:+PreserveFramePointer -XX:+PrintGCApplicationStoppedTime -XX:+PrintSafepointStatistics -XX:-UseBiasedLocking -XX:-UseCounterDecay -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
         * <p>
         * $ jps
         * Take the process pid and use it with:
         * <p>
         * $ PERF_COLLAPSE_OPTS="--kernel --tid" PERF_RECORD_FREQ=99 PERF_RECORD_SECONDS=10 PERF_MAP_OPTIONS=unfoldall ~/perf-map-agent/bin/perf-java-flames <pid>
         */
        //warmup it until C2 compiled
        for (int i = 0; i < 100_000; i++) {
            a();
        }
        Thread profiledRunner = new Thread(() -> {
            final Thread currentThread = Thread.currentThread();
            while (!currentThread.isInterrupted()) {
                a();
            }
        });
        profiledRunner.setDaemon(true);
        profiledRunner.start();
        try {
            System.out.println("press one key to stop it...");
            System.in.read();
        } finally {
            profiledRunner.interrupt();
        }
    }

    private static void a() {
        b();
    }

    private static void b() {
        c();
        d();
        int limit = WORKS;
        long k = 0;
        for (int l = 0; l < limit; l++) {
            k++;
            if ((k % 2) == 1)
                k += l;
        }
    }

    private static void c() {
        int limit = WORKS;
        long k = 0;
        for (int l = 0; l < limit; l++) {
            k++;
            if ((k % 2) == 1)
                k += l;
        }
    }

    private static void d() {
        int limit = WORKS;
        long k = 0;
        for (int l = 0; l < limit; l++) {
            k++;
            if ((k % 2) == 1)
                k += l;
        }
        for (int l = 0; l < limit; l++) {
            k++;
            if ((k % 2) == 1)
                k += l;
        }
    }
}
