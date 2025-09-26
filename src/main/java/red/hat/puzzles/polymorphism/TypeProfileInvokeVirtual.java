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
 * i.e. passing a known concrete type as a parameter of an inlineable method, expecting the call site to become monomorphic
 * despite the method itself has previously compiled with a mega-morphic call-site.
 *
 * This benchmark can be run with -XX:CICompilerCount=1 -XX:-UseOnStackReplacement -XX:-TieredCompilation -XX:-BackgroundCompilation -jar
 * target/benchmarks.jar red.hat.puzzles.polymorphism.TypeProfileInvokeVirtual -f 0
 *
 * for debugging purposes
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

    @Param({"true"})
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
        B b = new B();
        C c = new C();
        D d = new D();
        if (polluteTypeProfile) {
            instances[1] = b;
            instances[2] = c;
            instances[3] = d;
        } else {
            instances[1] = concreteTypeInstance;
            instances[2] = concreteTypeInstance;
            instances[3] = concreteTypeInstance;
        }
        for (int warmup = 0; warmup < 11_000; warmup++) {
            for (int i = 0; i < instances.length; i++) {
                bh.consume(doModulus(instances[i], params[i]));
            }
        }
        System.out.println("WARMUP COMPLETED!");
    }

    /**
     * We end up optimizing Base::base during the compilation of this method, while inlining doModulus and the call to Base::base
     * <pre>
     * Parse::do_call doCall.cpp:588                    <------ before optimizing virtual call Base::base it uses receiverNode
     * Parse::do_one_bytecode parse2.cpp:2723
     * Parse::do_one_block parse1.cpp:1593
     * Parse::do_all_blocks parse1.cpp:724
     * Parse::Parse parse1.cpp:628                      <------ doModulus
     * ParseGenerator::generate callGenerator.cpp:97
     * Parse::do_call doCall.cpp:677
     * Parse::do_one_bytecode parse2.cpp:2723
     * Parse::do_one_block parse1.cpp:1593
     * Parse::do_all_blocks parse1.cpp:724
     * Parse::Parse parse1.cpp:628                      <------ modulusSpeculativeTypeStaticFinal
     * ParseGenerator::generate callGenerator.cpp:97
     * Compile::Compile compile.cpp:806
     * C2Compiler::compile_method c2compiler.cpp:147
     * CompileBroker::invoke_compiler_on_method compileBroker.cpp:2345
     * CompileBroker::compiler_thread_loop compileBroker.cpp:1989
     * CompilerThread::thread_entry compilerThread.cpp:69
     * JavaThread::thread_main_inner javaThread.cpp:775
     * JavaThread::run javaThread.cpp:760
     * Thread::call_run thread.cpp:243
     * thread_native_entry os_linux.cpp:898
     *</pre>
     * and will end up into https://github.com/openjdk/jdk/blob/b73663a2b4fe7049fc0990c1a1e51221640b4e29/src/hotspot/share/opto/doCall.cpp#L1185
     * which is making through the CHA analysis to determine that the call is monomorphic.
     *
     * The key point is that https://github.com/openjdk/jdk/blob/b73663a2b4fe7049fc0990c1a1e51221640b4e29/src/hotspot/share/opto/doCall.cpp#L587-L598
     * found the concrete type {@code TypeProfileInvokeVirtual} in the receiver_node while performing do_call of base
     * in the inlined doModulus context:
     * <pre>
     *     Node* receiver_node             = stack(sp() - nargs);
     * </pre>
     * This information is then passed from optimize_virtual_call to optimize_inlining till
     * the mentioned https://github.com/openjdk/jdk/blob/b73663a2b4fe7049fc0990c1a1e51221640b4e29/src/hotspot/share/opto/doCall.cpp#L1185.
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeStaticFinal() {
        return doModulus(SINGLETON_CONCRETE_TYPE_INSTANCE, 3);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeInstanceField() {
        return doModulus(concreteTypeInstance, 3);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusSpeculativeTypeThis() {
        return doModulus(this, 3);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int modulusNoSpeculativeTypeInstanceField() {
        return doModulus(abstractTypeInstance, 3);
    }
}
