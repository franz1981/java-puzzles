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
import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class AnonymousRunnableBenchmark {

    private AtomicReference<Runnable> taskHolder;

    private static final class BlackHoleRunnable implements Runnable {

        private final Blackhole bh;

        private BlackHoleRunnable(Blackhole bh) {
            this.bh = bh;
        }

        @Override
        public void run() {
            bh.consume(this);
        }
    }

    @Setup
    public void init() {
        taskHolder = new AtomicReference<>();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void anonymousRunnable(Blackhole bh) {
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                bh.consume(this);
            }
        };
        taskHolder.lazySet(task);
        taskHolder.get().run();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void customRunnable(Blackhole bh) {
        final Runnable task = new BlackHoleRunnable(bh);
        taskHolder.lazySet(task);
        taskHolder.get().run();
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void lambda(Blackhole bh) {
        final Runnable task = () -> bh.consume(this);
        taskHolder.lazySet(task);
        taskHolder.get().run();
    }


    public static void main(String[] args) throws RunnerException {
        ArrayFillBenchmark.runBenchmark(AnonymousRunnableBenchmark.class);
    }
}
