package red.hat.puzzles.profilers;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 20, time = 1)
@Fork(1)
public class ProfilerFallacy {

    @Param("1000")
    private int work;
    private byte[] bytes;
    private byte v;

    @Setup
    public final void setup() {
        bytes = new byte[work];
        v = 0x1;
    }

    private void blameMePlease() {
        notInlineableCall();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void notInlineableCall() {

    }

    /**
     * This is explained on: https://github.com/jvm-profiling-tools/async-profiler/issues/127
     * JDK 8/9 that don't report correctly the BCI of Arrays::fill native body (ie arrayof_jbyte_fill),
     * wrongly attributing it to blameMePlease@-1.
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void arrayFillAsyncProfilerBlameTheNearestBciOnJdk8() {
        Arrays.fill(bytes, v);
        blameMePlease();
    }

    /**
     * It fails both on async profiler and visualVM, but not on perf
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void doIntrinsicAndBlameThePollReturnOwner() {
        System.arraycopy(bytes, 0, bytes, 0, bytes.length);
        blameMePlease();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void blameTheWrongMethodInsteadOfTheCountedLoopOnJdk8() {
        countedLoop(work);
        //no poll or poll_return on countedLoop
        blameMePlease();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void blameTheWrongMethodInsteadOfTheCountedLoopOnJdk11() {
        countedLoop(100);
        //no poll or poll_return on countedLoop
        blameMePlease();
    }

    private static volatile long consumedCPU = System.nanoTime();

    private static void countedLoop(int tokens) {
        long t = consumedCPU;

        for (int i = 0; i < tokens; i++) {
            t += (t * 0x5DEECE66DL + 0xBL + i) & (0xFFFFFFFFFFFFL);
        }

        // Need to guarantee side-effect on the result, to dodge DCE.
        if (t == 0x2A) {
            consumedCPU += t;
        }
    }
}
