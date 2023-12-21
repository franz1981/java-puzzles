package red.hat.puzzles.polymorphism;

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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/*
 * Original gist: https://gist.github.com/franz1981/e46823dbaeb576c1a3344683b2319db8
 *
 * Run me with: --jvmArgs="-XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*.closeNotAutoCloseable"
 *
 * This is meant to verify that https://github.com/netty/netty/issues/13745 has improved the numbers,
 * by adding 2 type guards for the most probable cases.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@State(Scope.Benchmark)
public class TypeCheckBenchmark {

    private Object[] instances;

    @Param({"0", "1", "2", "3"})
    private int types;
    private int index;

    @Setup
    public void setup(Blackhole bh) {
        instances = new Object[1 + types];
        switch (types) {
            case 0:
                instances[0] = ManySecondarySuperTypes.Instance;
                break;
            case 1:
                instances[0] = ManySecondarySuperTypes.Instance;
                instances[1] = ManySecondarySuperTypes1.Instance;
                break;
            case 2:
                instances[0] = ManySecondarySuperTypes.Instance;
                instances[1] = ManySecondarySuperTypes1.Instance;
                instances[2] = ManySecondarySuperTypes2.Instance;
                break;
            case 3:
                instances[0] = ManySecondarySuperTypes.Instance;
                instances[1] = ManySecondarySuperTypes1.Instance;
                instances[2] = ManySecondarySuperTypes2.Instance;
                instances[3] = ManySecondarySuperTypes3.Instance;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + types);
        }
    }

    @Benchmark
    public boolean instanceofTypeCheck() {
        int nextIdx = index;
        index++;
        if (index == types + 1) {
            index = 0;
        }
        return closeNotAutoCloseable(instances[nextIdx]);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static boolean closeNotAutoCloseable(Object o) {
        // it searches through the secondary supers (i.e., an array of objects) for a type match
        // but does not find one since "o" is not an "AutoCloseable" type
        if (o instanceof AutoCloseable) {
            try {
                ((AutoCloseable) o).close();
                return true;
            } catch (Exception ignore) {
                return false;
            }
        } else {
            // it always takes this slow path
            return false;
        }
    }

    public interface I1 {
    }

    public interface I2 {
    }

    public interface I3 {
    }

    public interface I4 {
    }

    public interface I5 {
    }

    public interface I6 {
    }

    public interface I7 {
    }

    public interface I8 {
    }

    private enum ManySecondarySuperTypes implements I1, I2, I3, I4, I5, I6, I7, I8 {
        Instance
    }

    private enum ManySecondarySuperTypes1 implements I1, I2, I3, I4, I5, I6, I7, I8 {
        Instance
    }

    private enum ManySecondarySuperTypes2 implements I1, I2, I3, I4, I5, I6, I7, I8 {
        Instance
    }

    private enum ManySecondarySuperTypes3 implements I1, I2, I3, I4, I5, I6, I7, I8 {
        Instance
    }
}