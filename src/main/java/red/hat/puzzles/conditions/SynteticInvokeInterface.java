package red.hat.puzzles.conditions;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@State(Scope.Benchmark)
@Fork(value = 2, jvmArgsAppend = "-XX:MinJumpTableSize=10")
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class SynteticInvokeInterface {

    // https://stackoverflow.com/questions/44988841/force-tableswitch-instead-of-lookupswitch
    // We're forcing -XX:MinJumpTableSize=10 and construct the generated predicates by consequence
    private static class FixedSizedGeneratedPredicate10<T> implements Predicate<T> {

        private static final AtomicInteger predicateId = new AtomicInteger();

        private final int type;
        private final Predicate<T> predicate;

        public FixedSizedGeneratedPredicate10(Predicate<T> predicate) {
            this.type = predicateId.getAndIncrement();
            this.predicate = predicate;
        }

        @Override
        public boolean test(T o) {
            return switch (type) {
                case 0 -> predicate.test(o);
                case 1 -> predicate.test(o);
                case 2 -> predicate.test(o);
                case 3 -> predicate.test(o);
                case 4 -> predicate.test(o);
                case 5 -> predicate.test(o);
                case 6 -> predicate.test(o);
                case 7 -> predicate.test(o);
                case 8 -> predicate.test(o);
                case 9 -> predicate.test(o);
                default -> predicate.test(o);
            };
        }
    }

    private static class FixedSizedGeneratedPredicate11<T> implements Predicate<T> {

        private static final AtomicInteger predicateId = new AtomicInteger();

        private final int type;
        private final Predicate<T> predicate;

        public FixedSizedGeneratedPredicate11(Predicate<T> predicate) {
            this.type = predicateId.getAndIncrement();
            this.predicate = predicate;
        }

        @Override
        public boolean test(T o) {
            return switch (type) {
                case 0 -> predicate.test(o);
                case 1 -> predicate.test(o);
                case 2 -> predicate.test(o);
                case 3 -> predicate.test(o);
                case 4 -> predicate.test(o);
                case 5 -> predicate.test(o);
                case 6 -> predicate.test(o);
                case 7 -> predicate.test(o);
                case 8 -> predicate.test(o);
                case 9 -> predicate.test(o);
                default -> switch (type) {
                    case 10 -> predicate.test(o);
                    case 11 -> predicate.test(o);
                    case 12 -> predicate.test(o);
                    case 13 -> predicate.test(o);
                    case 14 -> predicate.test(o);
                    case 15 -> predicate.test(o);
                    case 16 -> predicate.test(o);
                    case 17 -> predicate.test(o);
                    case 18 -> predicate.test(o);
                    case 19 -> predicate.test(o);
                    default -> predicate.test(o);
                };
            };
        }
    }

    private static class FixedSizedGeneratedPredicateTiered20<T> implements Predicate<T> {

        private static final AtomicInteger predicateId = new AtomicInteger();

        private final int type;
        private final Predicate<T> predicate;

        public FixedSizedGeneratedPredicateTiered20(Predicate<T> predicate) {
            this.type = predicateId.getAndIncrement();
            this.predicate = predicate;
        }

        @Override
        public boolean test(T o) {
            return switch (type) {
                case 0 -> predicate.test(o);
                case 1 -> predicate.test(o);
                case 2 -> predicate.test(o);
                case 3 -> predicate.test(o);
                case 4 -> predicate.test(o);
                case 5 -> predicate.test(o);
                case 6 -> predicate.test(o);
                case 7 -> predicate.test(o);
                case 8 -> predicate.test(o);
                case 9 -> predicate.test(o);
                default -> predicate.test(o);
            };
        }
    }


    @Param({"default", "binary", "jmp", "tiered"})
    private String type;
    private Predicate<Integer> predicate3;
    private Predicate<Integer> predicate1;
    private Predicate<Integer> predicate2;
    private Integer arg;

    @Setup
    public void init() {
        switch (type) {
            case "default":
                // these are on purpose different ones!
                predicate1 = value -> value == null;
                predicate2 = value -> value == null;
                predicate3 = value -> value == null;
                if (Set.of(predicate1.getClass(), predicate2.getClass(), predicate3.getClass()).size() < 3) {
                    throw new IllegalStateException("predicates should belong to different specific concrete types!");
                }
                break;
            case "binary":
                FixedSizedGeneratedPredicate10.predicateId.set(0);
                predicate1 = new FixedSizedGeneratedPredicate10<>(value -> value == null);
                predicate2 = new FixedSizedGeneratedPredicate10<>(value -> value == null);
                predicate3 = new FixedSizedGeneratedPredicate10<>(value -> value == null);
                break;
            case "jmp":
                FixedSizedGeneratedPredicate11.predicateId.set(0);
                predicate1 = new FixedSizedGeneratedPredicate11<>(value -> value == null);
                predicate2 = new FixedSizedGeneratedPredicate11<>(value -> value == null);
                predicate3 = new FixedSizedGeneratedPredicate11<>(value -> value == null);
                break;
            case "tiered":
                FixedSizedGeneratedPredicateTiered20.predicateId.set(0);
                predicate1 = new FixedSizedGeneratedPredicateTiered20<>(value -> value == null);
                predicate2 = new FixedSizedGeneratedPredicateTiered20<>(value -> value == null);
                predicate3 = new FixedSizedGeneratedPredicateTiered20<>(value -> value == null);
                break;
        }
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
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void eval(Blackhole bh) {
        bh.consume(eval(predicate1, arg));
        bh.consume(eval(predicate2, arg));
        bh.consume(eval(predicate3, arg));
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void inlinedEval(Blackhole bh) {
        bh.consume(inlinedEval(predicate1, arg));
        bh.consume(inlinedEval(predicate2, arg));
        bh.consume(inlinedEval(predicate3, arg));
    }

}
