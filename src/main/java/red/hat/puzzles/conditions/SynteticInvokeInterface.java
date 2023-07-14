package red.hat.puzzles.conditions;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import red.hat.puzzles.conditions.SynteticInvokeInterface.GeneratedPredicate.Type;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class SynteticInvokeInterface {

    public static class GeneratedPredicate implements Predicate<Integer> {

        public enum Type {
            getInt, getLong, getString
        }

        private final Type type;
        private final Predicate<Integer> predicate;

        public GeneratedPredicate(Type type, Predicate<Integer> predicate) {
            this.type = type;
            this.predicate = predicate;
        }

        @Override
        public boolean test(Integer integer) {
            switch (type) {

            case getInt -> {
                // how many types? 1!!!!
                // is inlined!
                return predicate.test(integer);
            }
            case getLong -> {
                return predicate.test(integer);
            }
            case getString -> {
                return predicate.test(integer);
            }
            }
            throw new AssertionError();
        }
    }

    private Predicate<Integer> getInt;
    private Predicate<Integer> getLong;
    private Predicate<Integer> getString;
    private Predicate<Integer> customGetInt;
    private Predicate<Integer> customGetLong;
    private Predicate<Integer> customGetString;
    private Integer arg;

    @Setup
    public void init() {
        getInt = value -> value == null;
        getLong = value -> value == null;
        getString = value -> value == null;
        customGetInt = new GeneratedPredicate(Type.getInt, value -> value == null);
        customGetLong = new GeneratedPredicate(Type.getLong, value -> value == null);
        customGetString = new GeneratedPredicate(Type.getString, value -> value == null);
    }

    public static <T> boolean inlinedEval(Predicate<T> predicate, T value) {
        return predicate.test(value);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static <T> boolean eval(Predicate<T> predicate, T value) {
        // invokeInterface
        // what's the arity?
        return predicate.test(value);
    }

    @Benchmark
    public void eval(Blackhole bh) {
        bh.consume(eval(getInt, arg));
        bh.consume(eval(getLong, arg));
        bh.consume(eval(getString, arg));
    }

    @Benchmark
    public void inlinedEval(Blackhole bh) {
        bh.consume(inlinedEval(getInt, arg));
        bh.consume(inlinedEval(getLong, arg));
        bh.consume(inlinedEval(getString, arg));
    }

    @Benchmark
    public void customEval(Blackhole bh) {
        bh.consume(eval(customGetInt, arg));
        bh.consume(eval(customGetLong, arg));
        bh.consume(eval(customGetString, arg));
    }

    @Benchmark
    public void customInlinedEval(Blackhole bh) {
        bh.consume(inlinedEval(customGetInt, arg));
        bh.consume(inlinedEval(customGetLong, arg));
        bh.consume(inlinedEval(customGetString, arg));
    }

}
