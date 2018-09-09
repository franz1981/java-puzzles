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

package red.hat.puzzles.combiner;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class CombinerBench {


    //hardcoded but can be changed :)
    private static final int THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;
    private static final int TOKENS = 10;
    private static final int MAX_SPINS = 256;

    @Param({"vanilla", "unsafe", "lock"})
    private String combinerType;
    @Param({"spin", "yield", "park"})
    private String idleStrategyType;
    private Combiner.IdleStrategy idleStrategy;

    private Runnable combinerAction;
    private Combiner combiner;

    @Setup
    public void init() {
        switch (idleStrategyType) {
            case "spin":
                idleStrategy = Combiner.IdleStrategy.busySpin();
                break;
            case "yield":
                idleStrategy = Combiner.IdleStrategy.yield(MAX_SPINS);
                break;
            case "park":
                idleStrategy = Combiner.IdleStrategy.park(MAX_SPINS);
                break;
            default:
                throw new AssertionError("unsupported idleStrategyType: " + idleStrategyType);
        }
        combinerAction = () -> Blackhole.consumeCPU(TOKENS);
        switch (combinerType) {
            case "vanilla":
                combiner = new VanillaCombiner(THREAD_LIMIT);
                break;
            case "unsafe":
                combiner = new OptimizedCombiner(THREAD_LIMIT);
                break;
            case "lock":
                //emulate a non cooperative way to perform the action on the shared resource
                final AtomicLong spinLock = new AtomicLong(0);
                combiner = (action, idleStrategy) -> {
                    int idleCount = 0;
                    //only the first one that is able to 0->1 is the one that has rights to enter the lock
                    while (spinLock.getAndIncrement() != 0) {
                        idleCount = idleStrategy.idle(idleCount);
                    }
                    try {
                        action.run();
                    } finally {
                        //just use relaxed op, because we are not interested to the full fat lock semantic
                        //on the guarded region
                        spinLock.lazySet(0);
                    }
                };
                break;
            default:
                throw new AssertionError("unsupported combinerType: " + combinerType);
        }

    }

    @GroupThreads(4)
    @Benchmark
    public void combine() {
        combiner.combine(combinerAction, idleStrategy);
    }

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(CombinerBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }


}
