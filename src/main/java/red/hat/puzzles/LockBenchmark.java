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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class LockBenchmark {

    @Param({"1", "100"})
    private int tokens;
    private Object monitor;
    private Lock lock;

    @Setup
    public void init() {
        monitor = new Object();
        lock = new ReentrantLock();
    }

    @Benchmark
    @GroupThreads(3)
    public void intrinsticLock() {
        synchronized (monitor) {
            Blackhole.consumeCPU(tokens);
        }
    }

    @Benchmark
    @GroupThreads(3)
    public void jucLock() {
        lock.lock();
        try {
            Blackhole.consumeCPU(tokens);
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(LockBenchmark.class.getSimpleName())
                .jvmArgs("-XX:-UseBiasedLocking")
                .build();
        new Runner(opt).run();
    }

}
