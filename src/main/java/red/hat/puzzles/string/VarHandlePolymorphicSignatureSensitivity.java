package red.hat.puzzles.string;

import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(2)
public class VarHandlePolymorphicSignatureSensitivity {

    private static final VarHandle COUNTER_VH;

    static {
        try {
            COUNTER_VH = MethodHandles.lookup().findVarHandle(
                    VarHandlePolymorphicSignatureSensitivity.class,
                    "counter",
                    long.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private long counter;
    private String name;

    @Benchmark
    public String wrongVarHandleConcatIndy() {
        return name + (long) COUNTER_VH.getAndAdd(this, 1);
    }

    @Benchmark
    public String rightVarHandleConcatIndy() {
        return name + (long) COUNTER_VH.getAndAdd(this, 1L);
    }

    @Benchmark
    public long wrongVarHandle() {
        return (long) COUNTER_VH.getAndAdd(this, 1);
    }

    @Benchmark
    public long rightVarHandle() {
        return (long) COUNTER_VH.getAndAdd(this, 1L);
    }
}
