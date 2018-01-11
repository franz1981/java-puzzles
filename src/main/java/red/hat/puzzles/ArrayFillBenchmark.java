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

import org.agrona.UnsafeAccess;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Shows Perf tools and JMH usage + Running with scissors with Unsafe is not always a Perf win :P
 *
 * NOTE:
 * - automatic blackhole on return
 * - DONT_INLINE work only on the higher level declaration
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ArrayFillBenchmark {

    private byte[] bytes;
    private long[] longBytes;
    private int size;
    private byte b;

    @Setup
    public void init() {
        size = 32 * 1024;
        bytes = new byte[size];
        longBytes = new long[size / Long.BYTES];
        b = 1;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public byte[] fill() {
        //Arrays::fill replacement rules:
        //http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/61746b5f0ed3/src/cpu/x86/vm/macroAssembler_x86.cpp#l6085
        //vmovdqu
        Arrays.fill(bytes, b);
        return bytes;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public long[] fillLong() {
        //Superword Level Parallelism (SLP):
        //http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/55fb97c4c58d/src/share/vm/opto/superword.cpp
        //play with: -XX:-UseSuperWord -XX:-OptimizeFill
        //TLDR: Loop unrolling -> loop level parallelism into ILP = SuperWord Optimization -> vector parallelism into SLP
        Arrays.fill(longBytes, b);
        return longBytes;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public long[] handrolledReverse() {
        //JVM can't recognize the IR pattern
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = b;
        }
        return longBytes;
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public byte[] handrolled() {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = b;
        }
        return bytes;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public byte[] maybeMemset() {
        //it could be used Copy::fill_to_memory_atomic
        //https://github.com/JetBrains/jdk8u_hotspot/blob/master/src/share/vm/utilities/copy.cpp#L58
        UnsafeAccess.UNSAFE.setMemory(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, size, b);
        return bytes;
    }


    public static void main(String[] args) throws RunnerException {
        runBenchmark(ArrayFillBenchmark.class);
    }


    public static void runBenchmark(Class<?> benchmarkClass) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(benchmarkClass.getSimpleName())
                .addProfiler(LinuxPerfAsmProfiler.class)
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

}
