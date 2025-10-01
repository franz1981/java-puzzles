package red.hat.puzzles.checks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Fork(2)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class BoundChecks {


    public static boolean branchfullIsOutOfBounds(int index, int length, int capacity) {
        if ((index | length | capacity | (index + length)) < 0) {
            return true;
        }
        return (index + length > capacity);
    }

    public static boolean branchlessIsOutOfBounds(int index, int length, int capacity) {
        return (index | length | capacity | (index + length) | (capacity - (index + length))) < 0;
    }

    int index;
    int length;
    int capacity;

    @Setup
    public void setup() {
        index = 0;
        length = 1;
        capacity = 3;
    }

    @Benchmark
    public boolean branchfull() {
        return branchfullIsOutOfBounds(index, length, capacity);
    }

    @Benchmark
    public boolean branchless() {
        return branchlessIsOutOfBounds(index, length, capacity);
    }

}
