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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * http://psy-lob-saw.blogspot.com/2016/02/why-most-sampling-java-profilers-are.html
 * Not very scientific but useful: use fork(0) if you want to profile it with jvisualVM with ease.
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class SafepointBiasFailure {

    @Param("1000")
    int size;
    byte[] bytes;
    boolean result;

    @Setup
    public final void setup() {
        bytes = new byte[size];
        result = true;
    }

    @Benchmark
    public void countedInlined() {
        byte b = 0;
        for (int i = 0; i < size; i++) {
            b += bytes[i];
        }
        result = b == 0;
        //No safepoint poll here (is inlined), the first {poll}
        //will be on the caller loop back edge:
        //the instruction next the poll will take
        //all the blame!!!
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void countedNotInlined() {
        byte b = 0;
        for (int i = 0; i < size; i++) {
            b += bytes[i];
        }
        result = b == 0;
        //{poll_return} here
        //the call-site of this method will take all the blame
        //instead of the method!!!
    }

    @Benchmark
    public void blameCallSite() {
        byte b = 0;
        for (int i = 0; i < size; i++) {
            b += bytes[i];
        }
        //No safepoint poll here
        setResultNotInlined(b == 0);
        //this call-site will take all the blame:
        //on the benchmark loop it will be the only safepoint poll
        //before the loop back edge, because blameCallSite
        //will get inlined
    }

    @Benchmark
    public void blameLastCallSite() {
        byte b = 0;
        for (int i = 0; i < size; i++) {
            b += bytes[i];
        }
        //No safepoint poll here
        setResultInlined(b == 0);
        //this method will take all the blame because it will be the caller
        //of a method containing a {poll_return}
    }

    private void setResultInlined(boolean b) {
        setResultNotInlined(b);
        //this call-site will take all the blame!
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void setResultNotInlined(boolean b) {
        result = b;
        //{poll_return} here: the call-site will be blamed
    }

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SafepointBiasFailure.class.getSimpleName())
                .jvmArgs("-XX:+UnlockDiagnosticVMOptions","-XX:+DebugNonSafepoints")
                //-prof stack:detailLine=true
                .addProfiler(StackProfiler.class, "detailLine=true")
                .build();
        new Runner(opt).run();
    }


}
