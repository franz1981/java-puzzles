package red.hat.puzzles.profilers;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(1)
public class TLABAllocationBias {

    /**
     * Run it with:
     *
     * red.hat.puzzles.profilers.TLABAllocationBias.* -prof gc --jvmArgs="-XX:+AlwaysPreTouch -Xms2G -Xmx2G -XX:+UseG1GC" -prof "async:output=flamegraph;event=alloc;dir=/tmp;libPath=/home/forked_franz/IdeaProjects/async-profiler/build/libasyncProfiler.so"
     */

    private int hopeFullyNotCached;

    @Setup
    public void init() {
        hopeFullyNotCached = 1000;
        Integer boxedFirst = hopeFullyNotCached;
        Integer boxedSecond = hopeFullyNotCached;
        if (boxedFirst == boxedSecond) {
            throw new IllegalArgumentException("Benchmark not valid! Integer cached values should be < " + hopeFullyNotCached);
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void smallByteArrayAndInteger(final Blackhole bh) {
        Integer notCached = hopeFullyNotCached;
        bh.consume(notCached);
        bh.consume(new byte[10]);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void bigByteArrayAndInteger(final Blackhole bh) {
        Integer notCached = hopeFullyNotCached;
        bh.consume(notCached);
        bh.consume(new byte[1024]);
    }
}
