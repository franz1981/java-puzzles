package red.hat.puzzles.itable;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(2)
public class ItableVarHandleUnsafe {

    private static final int SAMPLES = Integer.getInteger("id.samples", 32 * 1024);
    private static final int SEED = 42;

    private IdPresence[] elements;
    private long nextId;
    private int bogusId;


    @Setup
    public void setup() {
        if (Integer.bitCount(SAMPLES) != 1) {
            throw new IllegalArgumentException("SAMPLES must be a power of 2");
        }
        IdPresence[] types = new IdPresence[]{new EntityA(), new EntityB(), new EntityC()};
        Random r = new Random(SEED);
        elements = new IdPresence[SAMPLES];
        this.bogusId = r.nextInt();
        for (int i = 0; i < SAMPLES; i++) {
            IdPresence type = types[r.nextInt(types.length)];
            elements[i] = type;
        }
    }

    private IdPresence nextElement() {
        return elements[(int) (nextId++ % SAMPLES)];
    }

    @Benchmark
    public int getIdInvokeInterface() {
        IdPresence element = nextElement();
        return element.getId();
    }

    @Benchmark
    public int getIdVarHandle() {
        IdPresence element = nextElement();
        return VarHandleIdStorage.getId(element);
    }

    @Benchmark
    public int getIdUnsafe() {
        IdPresence element = nextElement();
        return UnsafeIdStorage.getId(element);
    }

    @Benchmark
    public IdPresence setIdInvokeInterface() {
        IdPresence element = nextElement();
        element.setId(bogusId);
        return element;
    }

    @Benchmark
    public IdPresence setIdInvokeVarHandle() {
        IdPresence element = nextElement();
        VarHandleIdStorage.setId(element, bogusId);
        return element;
    }

    @Benchmark
    public IdPresence setIdUnsafe() {
        IdPresence element = nextElement();
        UnsafeIdStorage.setId(element, bogusId);
        return element;
    }


}
