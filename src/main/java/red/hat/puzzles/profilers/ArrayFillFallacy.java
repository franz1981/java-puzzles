package red.hat.puzzles.profilers;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * It has been explained here: https://github.com/jvm-profiling-tools/async-profiler/issues/127
 * It is an issue of JDK 8/9 that do not report correctly the BCI of Arrays::fill native body (ie arrayof_jbyte_fill),
 * wrongly attributing it to blameMePlease@-1.
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(1)
public class ArrayFillFallacy {

    @Param("1000")
    int size;
    byte[] bytes;
    byte v;

    @Setup
    public final void setup() {
        bytes = new byte[size];
        v = 0x1;
    }


    @Benchmark
    public void arrayFillAndBlameTheCallerOfThePollExit() {
        Arrays.fill(bytes, v);
        blameMePlease();
    }

    private void blameMePlease() {
        notInlineableCall();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void notInlineableCall() {

    }


    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ArrayFillFallacy.class.getSimpleName())
                .jvmArgs("-XX:+UnlockDiagnosticVMOptions",
                         "-XX:+DebugNonSafepoints",
                         "-XX:+PreserveFramePointer")
                .addProfiler(LinuxPerfAsmProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
