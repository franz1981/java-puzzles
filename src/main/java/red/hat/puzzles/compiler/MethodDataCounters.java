package red.hat.puzzles.compiler;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class MethodDataCounters {



    @Benchmark
    @Threads(1)
    @Fork( value = 2, jvmArgsAppend = {"-XX:TieredStopAtLevel=1"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier1_1() {

    }

    @Benchmark
    @Threads(4)
    @Fork( value = 2, jvmArgsAppend = { "-XX:TieredStopAtLevel=1"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier1_4() {
    }

    @Benchmark
    @Threads(1)
    @Fork( value = 2, jvmArgsAppend = {"-XX:TieredStopAtLevel=3"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier3_1() {
    }

    @Benchmark
    @Threads(4)
    @Fork( value = 2, jvmArgsAppend = {"-XX:TieredStopAtLevel=3"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier3_4() {
    }

    @Benchmark
    @Threads(1)
    @Fork( value = 2, jvmArgsAppend = { "-XX:TieredStopAtLevel=4"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier4_1() {
    }

    @Benchmark
    @Threads(4)
    @Fork( value = 2, jvmArgsAppend = {"-XX:TieredStopAtLevel=4"})
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void invokeTier4_4() {
    }
}
