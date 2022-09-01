package red.hat.puzzles.instanceofscalability;

import org.openjdk.jmh.annotations.*;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class TraitsBenchmark {
    interface I1 {
        int foo1();
    }

    interface I2 {
        int foo2();
    }

    interface I3 {
        int foo3();
    }

    static class A implements I1, I2, I3 {

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 1;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 2;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 3;
        }
    }

    static class B implements I1, I2, I3 {
        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 4;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 5;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 6;
        }
    }

    static class C implements I1, I2, I3 {
        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 7;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 8;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 9;
        }
    }

    public enum Traits {
        I1,
        I2,
        I3
    }

    interface TraitsImplementor {
        Traits[] traits();

        default int foo1() {
            throw new UnsupportedOperationException();
        }

        default int foo2() {
            throw new UnsupportedOperationException();
        }

        default int foo3() {
            throw new UnsupportedOperationException();
        }
    }

    static class D implements TraitsImplementor {

        private final Traits[] traits = new Traits[]{Traits.I1, Traits.I2, Traits.I3};

        @Override
        public Traits[] traits() {
            return traits;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 1;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 2;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 3;
        }
    }

    static class E implements TraitsImplementor {

        private final Traits[] traits = new Traits[]{Traits.I1, Traits.I2, Traits.I3};

        @Override
        public Traits[] traits() {
            return traits;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 4;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 5;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 6;
        }
    }

    static class F implements TraitsImplementor {

        private final Traits[] traits = new Traits[]{Traits.I1, Traits.I2, Traits.I3};

        @Override
        public Traits[] traits() {
            return traits;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo1() {
            return 7;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo2() {
            return 8;
        }

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int foo3() {
            return 9;
        }
    }


    public Object a;
    public Object b;
    public Object c;

    public Object d;
    public Object e;
    public Object f;

    @Setup
    public void init() {
        a = new A();
        b = new B();
        c = new C();

        d = new D();
        e = new E();
        f = new F();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int instanceOfSum(Object o) {
        int sum = 0;
        if (o instanceof I1) {
            sum += ((I1) o).foo1();
        }
        if (o instanceof I2) {
            sum += ((I2) o).foo2();
        }
        if (o instanceof I3) {
            sum += ((I3) o).foo3();
        }
        return sum;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int traitsSum(Object o) {
        int sum = 0;
        if (o instanceof TraitsImplementor) {
            final TraitsImplementor impl = (TraitsImplementor) o;
            for (Traits traits : impl.traits()) {
                switch (traits) {
                    case I1:
                        sum += impl.foo1();
                        break;
                    case I2:
                        sum += impl.foo2();
                        break;
                    case I3:
                        sum += impl.foo3();
                        break;
                    default:
                        throw new AssertionError("IMPOSSIBLE");
                }
            }
        }
        return sum;
    }

    @Benchmark
    public int multiInstanceOfTraits() {
        return instanceOfSum(a) + instanceOfSum(b) + instanceOfSum(c);
    }

    @Benchmark
    public int traitsSet() {
        return traitsSum(d) + traitsSum(e) + traitsSum(f);
    }

}
