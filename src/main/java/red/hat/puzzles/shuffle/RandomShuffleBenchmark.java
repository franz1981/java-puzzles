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

    private static final ThreadLocal<RandomState> TL_STATE = ThreadLocal.withInitial(RandomState::new);

    @State(Scope.Thread)
    public static class RandomState {
        private final short CHOICES_LENGTH = MAX_VALUE + 1;
        private final short[] choices = new short[CHOICES_LENGTH];

        private short[] pooledUniques;

        {
            for (short i = 0; i < CHOICES_LENGTH; i++) {
                choices[i] = i;
            }
        }

        public short[] allocateRequired(int count) {
            final short[] pooled = this.pooledUniques;
            if (pooled == null || pooled.length < count) {
                final short[] newPooled = new short[count];
                this.pooledUniques = newPooled;
                return newPooled;
            }
            return pooled;
        }

        public short[] rollback(short[] uniqueRandom, short count) {
            final short[] choices = this.choices;
            if (count == CHOICES_LENGTH) {
                // let's find a cutoff value that can be used to decide when use this form :)
                for (short i = 0; i < CHOICES_LENGTH; i++) {
                    choices[i] = i;
                }
                return choices;
            }
            // rollback existing
            for (short i = 0; i < count; i++) {
                final short unique = uniqueRandom[i];
                choices[unique] = unique;
            }
            return choices;
        }

    }

    @Param({"5", "10", "15", "20"})
    private short count;

    private static final short MAX_VALUE = 10000;
    private static final Integer[] BOXED = new Integer[MAX_VALUE + 1];

    static {
        for (int i = 0; i < BOXED.length; i++) {
            BOXED[i] = i;
        }
    }

    @Benchmark
    public Integer[] threadLocalPooledFisherYatesBoxed() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final RandomState state = TL_STATE.get();
        final short count = this.count;
        return toBoxedResults(fisherYates(random, state, count), count);
    }

    private static Integer[] toBoxedResults(short[] results, short count) {
        final Integer[] boxed = new Integer[count];
        for (short i = 0; i < count; i++) {
            boxed[i] = BOXED[results[i]];
        }
        return boxed;
    }

    private static short[] fisherYates(ThreadLocalRandom random, RandomState state, short count) {
        if (count > state.CHOICES_LENGTH) {
            throw new IllegalArgumentException("cannot choose more values then ones on the existing range");
        }
        final short[] choices = state.choices;
        final short max = MAX_VALUE;
        final short[] uniqueRandom = state.allocateRequired(count);
        try {
            for (short i = 0; i < count; i++) {
                final short currentMax = (short) (max - i);
                final short nextToShuffle = (short) random.nextInt(0, currentMax + 1);
                final short nextUnique = choices[nextToShuffle];
                choices[nextToShuffle] = currentMax;
                uniqueRandom[i] = nextUnique;
            }
        } finally {
            state.rollback(uniqueRandom, count);
        }
        return choices;
    }

    @Benchmark
    public Integer[] fisherYatesBoxed(RandomState state) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final short count = this.count;
        return toBoxedResults(fisherYates(random, state, count), count);
    }

    @Benchmark
    public short[] fisherYates(RandomState state) {
        return fisherYates(ThreadLocalRandom.current(), state, count);
    }

    @Benchmark
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
