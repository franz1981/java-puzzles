package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * See https://bugs.openjdk.org/browse/JDK-8180450 for more info.
 * This benchmark must run with both -t 1 and -t <available cores>.<br>
 * Suggested profilers are -prof perfnorm -prof perfasm -prof perfc2c
 *
 */
@State(Scope.Benchmark)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class InstanceOfContentionBenchmark {
    interface I1 {
    }

    interface I2 {
    }

    class A implements I1, I2{

    }

    class B implements I1, I2{

    }


    private Object a;
    private Object b;


    @Setup
    public void init() {
        a = new A();
        b = new B();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static int dirty(Object o) {
        int i = 0;
        // load Klass word of the instance
        // load Klass::_secondary_super_cache to perform fast path check;
        // see https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/cpu/x86/macroAssembler_x86.cpp#L5371
        // If the cached secondary type won't match (because invalidated by another one), we hit the slow path:
        // see https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/cpu/x86/macroAssembler_x86.cpp#L5451
        // that:
        // - read the length of the list of implemented interfaces
        // - scan them using the repnz scas x86 instruction
        // - update the secondary cache type if the search succeeded
        if (o instanceof I1) {
            // invalidate the cache
            // update the cache
            i++;
            if (o instanceof I2) {
                // invalidate the cache
                // update the cache
                i++;
            }
        }
        return i;
    }

    @Benchmark
    public int checkOneType() {
        return dirty(a) + dirty(a) + dirty(a) + dirty(a);
    }

    @Benchmark
    public int checkTwoTypes() {
        // we use 2 types to pollute the type profiling of the dirty method
        // and perform the type check for real!
        return dirty(a) + dirty(b) + dirty(a) + dirty(b);
    }

}
