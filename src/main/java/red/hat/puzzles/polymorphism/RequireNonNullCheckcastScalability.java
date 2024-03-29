package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
// try running this with -XX:TypeProfileLevel=222 too: isDuplicated1() would be profiled to be DuplicatedContext and override the polluted profile at the checkcasts.
public class RequireNonNullCheckcastScalability {
    public interface InternalContext extends Context {
        // Internal Framework API
        boolean isDuplicated();
    }

    public interface Context {
        // some public API
    }

    public static class DuplicatedContext implements InternalContext {


        @Override
        public boolean isDuplicated() {
            return true;
        }
    }

    public static class NonDuplicatedContext implements InternalContext {

        @Override
        public boolean isDuplicated() {
            return false;
        }
    }

    public static class AlienNotDuplicatedContext implements Context {

    }

    private Context msg;

    @Param({"false", "true"})
    public boolean typePollution;
    @Param({"false", "true"})
    public boolean typePollutionNotInternalType;

    @Setup
    public void init(Blackhole bh) {
        if (typePollution) {
            msg = new NonDuplicatedContext();
            for (int i = 0; i < 11000; i++) {
                bh.consume(isDuplicated(msg));
            }
        }
        if (typePollutionNotInternalType) {
            msg = new AlienNotDuplicatedContext();
            for (int i = 0; i < 11000; i++) {
                bh.consume(isDuplicated(msg));
            }
        }
        msg = new DuplicatedContext();
    }

    // How to fix it?
    // Replace it with:
    //
    // return Objects.requireNonNull((InternalContext) message).isDuplicated();
    private static boolean isDuplicated(Context message) {
        Context actual = Objects.requireNonNull(message);
        if (actual instanceof InternalContext) {
            ((InternalContext) actual).isDuplicated();
        }
        return false;
    }

    @Benchmark
    @Threads(1)
    public boolean isDuplicated1() {
        return isDuplicated(msg);
    }

    @Benchmark
    @Threads(2)
    public boolean isDuplicated2() {
        return isDuplicated(msg);
    }

    @Benchmark
    @Threads(3)
    public boolean isDuplicated3() {
        return isDuplicated(msg);
    }

    @Benchmark
    @Threads(4)
    public boolean isDuplicated4() {
        return isDuplicated(msg);
    }

    @Benchmark
    @Threads(5)
    public boolean isDuplicated5() {
        return isDuplicated(msg);
    }

    @Benchmark
    @Threads(6)
    public boolean isDuplicated6() {
        return isDuplicated(msg);
    }
}
