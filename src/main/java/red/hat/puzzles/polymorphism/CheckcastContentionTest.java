package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class CheckcastContentionTest {
    interface I1 {
    }

    interface I2 {
    }

    class A implements I1, I2{

    }

    class B implements I1, I2{

    }

    @Param({"0", "1000", "10000"})
    public int typePoisoning;

    private List<I1> i1;
    private List<I2> i2;


    @Setup
    public void init(Blackhole bh) {
        i1 = new ArrayList<>(1);
        i2 = new ArrayList<>(1);
        if (typePoisoning > 0) {
            i1.add(new B());
            i2.add(new B());
        }
        for (int i = 0; i < typePoisoning; i++) {
            iterateI1(bh);
            iterateI2(bh);
        }
        // right now it could be biased just toward B!
        i1.clear();
        i2.clear();
        i1.add(new A());
        i2.add(new A());
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public void iterateI1(Blackhole bh) {
        for (I1 i : i1) {
           bh.consume(i);
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public void iterateI2(Blackhole bh) {
        for (I2 i : i2) {
            bh.consume(i);
        }
    }

}
