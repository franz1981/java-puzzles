package red.hat.puzzles.polymorphism;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class FalseSharingInstanceOfBenchmark {

    public interface ValueHolder {
        int value();
    }

    public interface NameHolder {
        String name();
    }

    private static class ConcreteMix implements ValueHolder, NameHolder {


        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int value() {
            return 3;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public String name() {
            return null;
        }
    }

    public static class PollutionConcreteMix implements ValueHolder, NameHolder {


        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int value() {
            return 7;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public String name() {
            return null;
        }
    }

    @Param({"0", "20000"})
    private int typePollution;
    private Object o;

    @Setup
    public void init(Blackhole bh) {
        o = new ConcreteMix();
        final Object pollution = new PollutionConcreteMix();
        // using all types would poison the type profile:
        // it's still a bit noisy because code shape depends on frequencies of types
        for (int i = 0; i < typePollution; i++) {
            bh.consume(getName(pollution));
            bh.consume(getName(o));
            bh.consume(getValue(pollution));
            bh.consume(getValue(o));
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static String getName(Object o) {
        if (o instanceof NameHolder) {
            return ((NameHolder) o).name();
        }
        return null;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static int getValue(Object o) {
        if (o instanceof ValueHolder) {
            return ((ValueHolder) o).value();
        }
        return -1;
    }

    @Benchmark
    @Group("falseSharing")
    @GroupThreads(1)
    public String instanceOfNameHolder() {
        return getName(o);
    }

    @Benchmark
    @Group("falseSharing")
    @GroupThreads(1)
    public int instanceOfValueHolder() {
        return getValue(o);
    }

}
