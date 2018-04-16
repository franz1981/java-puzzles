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

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class AnonymousRunnableBenchmark {

    private static final int WORK = 1;

    private static final class BlackHoleRunnable implements Runnable {

        private BlackHoleRunnable() {
        }

        @Override
        public void run() {
            Blackhole.consumeCPU(WORK);
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Runnable anonymousRunnable() {
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                Blackhole.consumeCPU(WORK);
            }
        };
        task.run();
        return task;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Runnable customRunnable() {
        final Runnable task = new BlackHoleRunnable();
        task.run();
        return task;
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Runnable lambda() {
        final Runnable task = () -> Blackhole.consumeCPU(WORK);
        task.run();
        return task;
    }


    public static void main(String[] args) throws RunnerException {
        ArrayFillBenchmark.runBenchmark(AnonymousRunnableBenchmark.class);
    }
}
