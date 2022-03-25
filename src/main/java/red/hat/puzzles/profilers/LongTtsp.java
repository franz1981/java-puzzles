package red.hat.puzzles.profilers;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 15)
@Fork(1)
public class LongTtsp {

    /**
     * Writing counted loop isn't easy anymore because of: http://cr.openjdk.java.net/~roland/loop_strip_mining.pdf
     */
    private byte[] from;
    private byte[] to;

    @Setup
    public void warmupCountedLoop(final Blackhole bh) throws InterruptedException {
        // this must be big in order to reduce the need of bigger ttspDelay that could interact
        // with the loop strip mining optimization
        from = new byte[100_000_000];
        to = new byte[100_000_000];
        byte[] f = new byte[1];
        byte[] t = new byte[1];
        // let's give both the chance to be compiled
        for (int i = 0; i < 20000; i++) {
            bh.consume(countedOperation(f, t));
        }
        for (int i = 0; i < 20000; i++) {
            bh.consume(globalSafepointOperation());
        }
        for (int i = 0; i < 20000; i++) {
            bh.consume(spinLoopFor(1));
        }
    }

    @Benchmark
    @Group("same")
    public long longOperationWithSafepointPoll() {
        return spinLoopFor(TimeUnit.MILLISECONDS.toNanos(1));
    }

    @Benchmark
    @Group("same")
    public ThreadInfo[] globalSafepointOperation() throws InterruptedException {
        final long start = System.nanoTime();
        final ThreadInfo[] info = ManagementFactory.getThreadMXBean()
                .dumpAllThreads(false, false);
        sleepRemaining(start, TimeUnit.MILLISECONDS.toNanos(1));
        return info;
    }

    @Benchmark
    @Group("same")
    public long longOperationWithoutSafepoint() {
        return countedOperation(from, to);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static long spinLoopFor(final long durationNs) {
        final long start = System.nanoTime();
        while (true) {
            final long elapsed = System.nanoTime() - start;
            if (elapsed >= durationNs) {
                return elapsed;
            }
        }
    }

    private static void sleepRemaining(final long startNs, final long durationNs) throws InterruptedException {
        final long elapsed = System.nanoTime() - startNs;
        final long untilOneMillis = durationNs - elapsed;
        if (untilOneMillis > 0) {
            TimeUnit.NANOSECONDS.sleep(untilOneMillis);
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static int countedOperation(final byte[] from, final byte[] to) {
        System.arraycopy(from, 0, to, 0, from.length);
        return to[0];
    }

}
