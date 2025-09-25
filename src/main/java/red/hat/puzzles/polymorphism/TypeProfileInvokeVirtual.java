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

abstract class Base {

    protected int base = 42;

    public abstract int base();
}

/**
 * This benchmark has been created to understand patterns like the one at https://github.com/franz1981/netty/blob/6984ba79327c8d9e2c1d6c4d9cd3304f845021c3/common/src/main/java/io/netty/util/Recycler.java#L306
 * i.e. passing a known concrete type as a parameter of an inlineable method, expecting the call site to become
 * monomorphic, despite the method itself has been compiled with a type profile containing multiple types.
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@State(Scope.Benchmark)
public class TypeProfileInvokeVirtual extends Base {

    @Override
    public int base() {
        return base;
    }

    private static class B extends Base {

        @Override
        public int base() {
            return base;
        }
    }

    private static class C extends Base {

        @Override
        public int base() {
            return base;
        }
    }

    private static class D extends Base {

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

    private TypeProfileInvokeVirtual concreteTypeInstance;
    private Base abstractTypeInstance;
    private static final TypeProfileInvokeVirtual SINGLETON_CONCRETE_TYPE_INSTANCE = new TypeProfileInvokeVirtual();

    @Setup
    public void setup(Blackhole bh) {
        Base[] instances = new Base[4];
        int[] params = new int[]{3, 5, 7, 11};
        concreteTypeInstance = this;
        abstractTypeInstance = concreteTypeInstance;
        instances[0] = concreteTypeInstance;
        if (polluteTypeProfile) {
            instances[1] = new B();
            instances[2] = new C();
            instances[3] = new D();
        } else {
            instances[1] = instances[0];
            instances[2] = instances[0];
            instances[3] = instances[0];
        }
        // let's get doModulus compiled with a type profile containing 1 or 4 types in
        for (int warmup = 0; warmup < 100_000; warmup++) {
            for (int i = 0; i < instances.length; i++) {
                bh.consume(doModulus(instances[i], params[i]));
            }
        }
    }

    /**
     * doModulus contains a virtual call to Base.base() and during the warmup with type pollution
     * it's compiled with a type profile information on Base::base which contains 4 types.
     * Once modulusSpeculativeTypeStaticFinal is hot enough and is compiled, the doModulus call-site is evaluated for inlining
     * and its type profile information for Base::base is overridden by the speculative type A of its parameter.
     * <p>
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L674
     * the doModulus parameter type is propagated as speculative type: this happens in the caller
     * (modulusSpeculativeTypeStaticFinal) compilation once the doModulus call-site is processed
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L195-L214 is where the
     * inlining decision to inline doModulus into modulusSpeculativeTypeStaticFinal is made: it performs a deferred inlining decision
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/callGenerator.cpp#L524 is where the
     * refinement logic is applied while inlining doModulus into modulusSpeculativeTypeStaticFinal
     * i.e. it (re)uses the speculative type context creating a refined inlined version of doModulus
     * - on https://github.com/openjdk/jdk/blob/2aafda1968f3fc8902f7d146a1cba72998aeb976/src/hotspot/share/opto/doCall.cpp#L237-L246
     * the speculative type is used to treat the call to Base::base in the inlined version of doModulus as monomorphic
     * <p>
     * NOTE:
     * Why inlining is deferred?
     * Hotspot perform incremental inlining:
     * 1. to better exploit contextual type information
     * 2. avoid node explosion
     * 3. spread inlining work across compilation phases to reduce the memory pressure
     * <p>
     * But it doesn't mean the timing to happen in a separate time from the caller compilation, just delayed
     * later in the same process e.g. there's no need of "additional" invocations of a method to trigger inlining.
     *
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeStaticFinal() {
        return doModulus(SINGLETON_CONCRETE_TYPE_INSTANCE, 3);
    }

    /**
     * Same as modulusSpeculativeTypeStaticFinal but it needs to read the instance field out from the benchmark state
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeInstanceField() {
        return doModulus(concreteTypeInstance, 3);
    }

    /**
     * Same as modulusSpeculativeTypeInstanceField although this should be passed as a register parameter
     * and not read from an instance field
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeThis() {
        return doModulus(this, 3);
    }

    /**
     * It perform differently based if type pollution happens:
     * - if no type pollution, doModulus is compiled with a type profile containing only A: fast
     * - if type pollution, doModulus is compiled with a type profile containing 4 types,
     * but although base is always an A at runtime, the call to Base::base() is not treated as monomorphic
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusNoSpeculativeTypeInstanceField() {
        return doModulus(abstractTypeInstance, 3);
    }
}
