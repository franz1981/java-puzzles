package red.hat.puzzles.profilers;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ConstructorWithVolatileField {

    @Benchmark
    public AtomicLong newAtomicLong() {
        return new AtomicLong();
    }

    @Benchmark
    public AtomicLong newInitAtomicLong() {
        return new AtomicLong(9);
    }

}
