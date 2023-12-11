package red.hat.puzzles.reflection;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 15, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class CheckPrimitiveWrappers {

    @Param({"true", "false"})
    public boolean noPrimitives;
    private Class[] classes;
    private int index;


    @Setup
    public void setup() {
        index = 0;
        if (noPrimitives) {
            classes = new Class[]{Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Object.class};
        } else {
            classes = new Class[]{boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, Object.class};
        }
    }

    private Class<?> nextClass() {
        index++;
        if (index >= classes.length) {
            index = 0;
        }
        return classes[index];
    }

    @Benchmark
    public Class<?> lookupPrimitiveWrapper() {
        return lookupPrimitiveWrapper(nextClass());
    }

    @Benchmark
    public Class<?> lookupPrimitiveWrapperWithMap() {
        return PRIMITIVES_TO_WRAPPERS.get(nextClass());
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;

    static {
        PRIMITIVES_TO_WRAPPERS = new HashMap<>();
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
    }

    private static Class<?> lookupPrimitiveWrapper(final Class<?> entityType) {
        if (!entityType.isPrimitive()) {
            return entityType;
        }
        if (entityType == boolean.class) {
            return Boolean.class;
        } else if (entityType == char.class) {
            return Character.class;
        } else if (entityType == byte.class) {
            return Byte.class;
        } else if (entityType == short.class) {
            return Short.class;
        } else if (entityType == int.class) {
            return Integer.class;
        } else if (entityType == long.class) {
            return Long.class;
        } else if (entityType == float.class) {
            return Float.class;
        } else if (entityType == double.class) {
            return Double.class;
        }
        // this shouldn't really happen, but better be safe than sorry
        return entityType;
    }


}
