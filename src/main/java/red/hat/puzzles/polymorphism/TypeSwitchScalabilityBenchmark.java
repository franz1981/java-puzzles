package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

@State(Scope.Benchmark)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 15, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
public class TypeSwitchScalabilityBenchmark {

    private static final class SerializableIntGarbage implements IntSupplier, Serializable {
        private final int value;

        public SerializableIntGarbage(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }
    }

    private static final class SerializableInt implements IntSupplier, Serializable {
        private final int value;

        public SerializableInt(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static boolean serialize(Serializable o) {
        // NO-OP, we live dangerously here :P
        return true;
    }

    static boolean serialize(Object o) {
        return switch (o) {
            case Serializable value -> serialize(value);
            default -> false;
        };
    }

    static int getAsInt(Object o) {
        return switch (o) {
            case IntSupplier value -> value.getAsInt();
            default -> -1;
        };
    }

    private Object o;
    @Param({"false", "true"})
    private boolean typePollution;

    @Setup
    public void init() {
        if (typePollution) {
            o = new SerializableIntGarbage(0);
            for (int i = 0; i < 11000; i++) {
                doSerialize();
                doGetAsInt();
            }
            o = new SerializableInt(0);
            for (int i = 0; i < 11000; i++) {
                doSerialize();
                doGetAsInt();
            }
        }
        o = new SerializableInt(0);

    }

    @Benchmark
    @Group
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean doSerialize() {
        return serialize(o);
    }

    @Benchmark
    @Group
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int doGetAsInt() {
        return getAsInt(o);
    }

}
