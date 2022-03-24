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
     * red.hat.puzzles.profilers.TLABAllocationBias.* -prof gc --jvmArgs="-XX:+AlwaysPreTouch -Xms2G -Xmx2G -XX:+UseG1GC" --gc true
     */

    private boolean firedBigAllocation;

    @Setup(Level.Iteration)
    public void init() {
        firedBigAllocation = false;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void smallByteArrayAndIntegerWithInitialBigAllocation(final Blackhole bh) {
        bh.consume(new Integer(1000));
        bh.consume(new byte[10]);
        if (!firedBigAllocation) {
            firedBigAllocation = true;
            bh.consume(new byte[1024 * 1024 * 1024]);
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void smallByteArrayAndInteger(final Blackhole bh) {
        bh.consume(new Integer(1000));
        bh.consume(new byte[10]);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void bigByteArrayAndInteger(final Blackhole bh) {
        bh.consume(new Integer(1000));
        bh.consume(new byte[1024]);
    }
}
