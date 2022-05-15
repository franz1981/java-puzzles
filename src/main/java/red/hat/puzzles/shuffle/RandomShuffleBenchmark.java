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
@Measurement(iterations = 5, time = 1)
@Fork(value = 2)
public class RandomShuffleBenchmark {

    @State(Scope.Thread)
    public static class RandomState {
        private final short[] choices = new short[MAX_VALUE + 1];

        {
            for (short i = 0; i < choices.length; i++) {
                choices[i] = i;
            }
        }

        private short max = MAX_VALUE;

        public short[] rollback() {
            final short[] choices = this.choices;
            if (max == -1) {
                // let's find a cutoff value that can be used to decide when use this form :)
                for (short i = 0; i < choices.length; i++) {
                    choices[i] = i;
                }
                max = MAX_VALUE;
                return choices;
            }
            for (short i = (short) (max + 1); i <= MAX_VALUE; i++) {
                final short value = choices[i];
                choices[value] = value;
                choices[i] = i;
            }
            max = MAX_VALUE;
            return choices;
        }

    }

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
    public Integer[] pooledFisherYatesBoxed(RandomState state) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final short[] choices = state.rollback();
        final int count = this.count;
        short max = state.max;
        final Integer[] uniqueRandom = new Integer[count];
        for (short i = 0; i < count; i++) {
            final short nextToShuffle = (short) random.nextInt(0, max + 1);
            final short nextUnique = choices[nextToShuffle];
            choices[nextToShuffle] = max;
            choices[max] = nextUnique;
            uniqueRandom[i] = BOXED[nextToShuffle];
            max--;
        }
        state.max = max;
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

}
