package red.hat.puzzles.compiler;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * A loop reads a {@code static final} array and calls a method that is not inlined.
 * C2 builds the array's address twice in a row and overwrites one copy before it is read.
 *
 * <p>aarch64, JDK 25.0.2:
 * <pre>
 *   mov   x13, #0xffa8            ; {oop([I{0x000000045423ffa8})}
 *   movk  x13, #0x5423, lsl #16
 *   movk  x13, #4, lsl #32
 *   mov   x12, #0xffa8            ; {oop([I{0x000000045423ffa8})}
 *   movk  x12, #0x5423, lsl #16
 *   movk  x12, #4, lsl #32
 *   add   x10, x13, w10, sxtw #2
 *   ldr   w12, [x10, #0x10]
 * </pre>
 *
 * <p>Needs hsdis.
 * <pre>
 * java -jar target/benchmarks.jar ConstantRematerializedTwice -f 1 -wi 3 -i 1 \
 *   -jvmArgsAppend '-XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=quiet \
 *     -XX:CompileCommand=print,red.hat.puzzles.compiler.ConstantRematerializedTwice::copy'
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@Threads(1)
public class ConstantRematerializedTwice {

    private static final int[] A = new int[2];

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static void f() {
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int copy() {
        int r = 0;
        for (int i = 0; i < 64; i++) {
            r += A[i & 1];
            f();
        }
        return r;
    }
}
