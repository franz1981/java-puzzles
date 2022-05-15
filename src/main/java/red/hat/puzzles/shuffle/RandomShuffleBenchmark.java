package red.hat.puzzles.shuffle;

import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(value = 1, jvmArgs = "-XX:EliminateAllocationArraySizeLimit=10002")
public class RandomShuffleBenchmark {

    @Param({"5", "10", "15", "20"})
    private int count;

    private static final short MAX_VALUE = 10000;
    private static final Integer[] BOXED = new Integer[MAX_VALUE + 1];

    static {
        for (int i = 0; i < BOXED.length; i++) {
            BOXED[i] = i;
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Integer[] fisherYatesBoxed() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final short[] choices = new short[MAX_VALUE + 1];
        // do not initialize it and just use 0 as our NULL element instead
        final int count = this.count;
        short max = MAX_VALUE;
        final Integer[] uniqueRandom = new Integer[count];
        for (short i = 0; i < count; i++) {
            final short nextToShuffle = (short) random.nextInt(0, max + 1);
            final short nextUnique = choices[nextToShuffle];
            final short realUnique;
            // this can be made branchless too!
            if (nextUnique == 0) {
                realUnique = nextToShuffle;
            } else {
                realUnique = (short) (nextUnique - 1);
            }
            // adding 1 to save 0 to ever appear: it's our NULL value
            choices[nextToShuffle] = (short) (max + 1);
            uniqueRandom[i] = BOXED[realUnique];
            max--;
        }
        return uniqueRandom;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Set<Integer> uniqueRandomUsingSet() {
        Set<Integer> ids = new HashSet<>(count);
        int counter = 0;
        while (counter < count) {
            final int random = 1 + ThreadLocalRandom.current().nextInt(10000);
            counter += ids.add(BOXED[random]) ? 1 : 0;
        }
        return ids;
    }

    /**
     * Run the benchmark with -prof gc and it should report no allocations :(
     * EliminateAllocationArraySizeLimit should control, if the short[] won't escape,
     * that it won't be allocated, but...it doesn't seem the case: INVESTIGATE!
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public long allocationCheck() {
        final short[] choices = new short[MAX_VALUE + 1];
        for (short i = 0; i < choices.length; i++) {
            choices[i] = i;
        }
        long sum = 0;
        for (short s : choices) {
            sum += s;
        }
        return sum;
    }


}
