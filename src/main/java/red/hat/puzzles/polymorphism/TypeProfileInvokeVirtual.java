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

import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@State(Scope.Benchmark)
public class TypeProfileInvokeVirtual {

    private abstract static class Base {

        protected int base = 42;

        public int id() {
            return base;
        }

        public abstract int base();
    }

    private static class A extends Base {

        @Override
        public int id() {
            return doModulus(this, 3);
        }

        @Override
        public int base() {
            return base;
        }
    }

    private static class B extends Base {

        @Override
        public int id() {
            return doModulus(this, 5);
        }

        @Override
        public int base() {
            return base;
        }
    }

    private static class C extends Base {

        @Override
        public int id() {
            return doModulus(this, 7);
        }

        @Override
        public int base() {
            return base;
        }
    }

    private static class D extends Base {

        @Override
        public int id() {
            return doModulus(this, 11);
        }

        @Override
        public int base() {
            return base;
        }
    }

    private static int doModulus(Base b, int m) {
        return b.base() % m;
    }

    @Param({"false", "true"})
    public boolean polluteTypeProfile;

    public long nextId;
    private int mask;

    private Base[] instances;

    private static final A SINGLETON_A = new A();
    private static A UNTRUSTED_SINGLETON_A = new A();

    private Base next() {
        return instances[(int) (nextId++ & mask)];
    }

    @Setup
    public void setup(Blackhole bh) {
        instances = new Base[4];
        mask = instances.length - 1;
        instances[0] = new A();
        if (polluteTypeProfile) {
            instances[1] = new B();
            instances[2] = new C();
            instances[3] = new D();
        } else {
            instances[1] = instances[0];
            instances[2] = instances[0];
            instances[3] = instances[0];
        }
        // to do this right we need an acquire fence too
        for (int i = 0; i < 400_000; i++) {
            VarHandle.acquireFence();
            bh.consume(id());
        }
        // make all the same type i.e. A
        instances[1] = instances[0];
        instances[2] = instances[0];
        instances[3] = instances[0];
        nextId = 0;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int id() {
        return next().id();
    }

    /**
     * doModulus is a very tiny method that is already compiled (and likely inlined) into the different id() calls.
     * With type profile pollution it should observe all 4 types (A, B, C, D) equally.<br>
     * This benchmarking method is not warmed up in the setup phase and is going to use the already compiled version
     * of doModulus while is not yet warmed up itself.<br>
     * Once it is warmed-up it should be able to inline doModulus (which is very tiny) propagating the SINGLETON_A type
     * and inlining A::base call without any type guards (since SINGLETON_A is static final).<br>
     * The same should happen to the primitive argument "3" which is always the same value.<br>
     * The logic of this is at https://github.com/openjdk/jdk/blob/jdk-26%2B16/src/hotspot/share/opto/doCall.cpp#L572-L674
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulus() {
        return doModulus(SINGLETON_A, 3);
    }

    /**
     * Same as modulus but need to read the static field out of the benchmark state
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int untrustedModulus() {
        return doModulus(UNTRUSTED_SINGLETON_A, 3);
    }
}
