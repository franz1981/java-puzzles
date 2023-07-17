package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 15, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
public class CapturingLambda {
    @Param({"1", "2", "3"})
    private int values;
    private int[] setOfValues;
    private int index;
    private int reference;
    private BooleanSupplier[] suppliers;

    @Setup
    public void init() {
        setOfValues = new int[values];
        for (int i = 0; i < values; i++) {
            setOfValues[i] = i;
        }
        suppliers = new BooleanSupplier[values];
        for (int i = 0; i < values; i++) {
            int value = setOfValues[i];
            suppliers[i] = () -> this.reference == value;
        }
        reference = -1;
        if (values > 3) {
            System.err.println("This benchmark has been designed with max 3 values: the not-capturing test case is now invalid");
        }

    }

    private int nextValue() {
        index++;
        if (index == values) {
            index = 0;
        }
        return setOfValues[index];
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean captureAndInvoke(Blackhole bh) {
        var previousValue = nextValue();
        BooleanSupplier supplier = () -> this.reference == previousValue;
        bh.consume(supplier);
        return invoke(supplier);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean notCaptureAndInvoke(Blackhole bh) {
        var previousValue = nextValue();
        BooleanSupplier supplier;
        switch (previousValue) {
            case 0:
                supplier = () -> this.reference == 0;
                break;
            case 1:
                supplier = () -> this.reference == 1;
                break;
            case 2:
                supplier = () -> this.reference == 2;
                break;
            default:
                supplier = () -> this.reference == previousValue;
        }
        bh.consume(supplier);
        return invoke(supplier);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean pooledCaptureAndInvoke(Blackhole bh) {
        var previousValue = nextValue();
        BooleanSupplier supplier = suppliers[previousValue];
        bh.consume(supplier);
        return invoke(supplier);
    }


    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean invoke(BooleanSupplier supplier) {
        return supplier.getAsBoolean();
    }
}
