package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 15, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class BaseAbstractClassBenchmark {

    private interface A {
        int foo();
    }

    private static abstract class BaseA implements A {
        @Override
        public int foo() {
            return 1;
        }
    }

    private static class A1 extends BaseA {

    }

    private static class A2 extends BaseA {

    }

    private static class A3 extends BaseA {

    }

    public BaseA[] types;
    public int next;

    @Setup
    public void init() {
        types = new BaseA[]{new A1(), new A2(), new A3()};
        next = 0;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private A nextA() {
        next++;
        if (next == types.length) {
            next = 0;
        }
        return types[next];
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private BaseA nextBaseA() {
        next++;
        if (next == types.length) {
            next = 0;
        }
        return types[next];
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int interfaceCall() {
        final A a = nextA();
        return a.foo();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int baseCall() {
        final BaseA a = nextBaseA();
        return a.foo();
    }


}
