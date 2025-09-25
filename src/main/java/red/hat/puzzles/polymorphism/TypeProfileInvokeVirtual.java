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
     * doModulus contains a virtual call to Base.base() and during the warmup with type pollution
     * it's compiled with a type profile information on Base::base which contains 4 types.
     * Once modulus is hot enough and is compiled, the doModulus call-site is evaluated for inlining
     * and its type profile information for Base::base is overridden by the speculative type A of its parameter.
     *
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L674
     *   the doModulus parameter type is propagated as speculative type: this happens in the caller (modulus) compilation once the doModulus call-site is processed
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L195-L214 is where the
     *   inlining decision to inline doModulus into modulus is made: it performs a deferred inlining decision
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/callGenerator.cpp#L524 is where the
     *   refinement logic is applied while inlining doModulus into modulus i.e. it (re)uses the speculative type context
     *   creating a refined inlined version of doModulus
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L237-L246
     *   the speculative type is used to treat the call to Base::base in the inlined version of doModulus as monomorphic
     *
     * NOTE:
     * Why inlining is deferred?
     * Hotspot perform incremental inlining:
     * 1. to better exploit contextual type information
     * 2. avoid node explosion
     * 3. spread inlining work across compilation phases to reduce the memory pressure
     *
     * But it doesn't mean the timing to happen in a separate time from the caller compilation, just delayed
     * later in the same process e.g. there's no need of "additional" invocations of a method to trigger inlining.
     *
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
