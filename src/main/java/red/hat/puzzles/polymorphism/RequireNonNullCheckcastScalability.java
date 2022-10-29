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
public class RequireNonNullCheckcastScalability {
    public interface InternalHttpMessage extends HttpMessage {
        // Internal Framework API
        long unsafeSize();
    }

    public interface HttpMessage {
        // some public API
    }

    public static class HttpStatefullMessage implements InternalHttpMessage {

        private long dummyField = 1;

        @Override
        public long unsafeSize() {
            return dummyField;
        }
    }

    public static class HttpStatelessMessage implements InternalHttpMessage {

        private long dummyField = 2;

        @Override
        public long unsafeSize() {
            return dummyField;
        }
    }

    private HttpMessage msg;

    @Param({"false", "true"})
    public boolean typePollution;

    @Setup
    public void init(Blackhole bh) {
        if (typePollution) {
            msg = new HttpStatelessMessage();
            for (int i = 0; i < 11000; i++) {
                bh.consume(unsafeTotalSize());
            }
            // deopt on warmup
        }
        msg = new HttpStatefullMessage();
    }

    // How to fix it?
    // Replace it with:
    //
    // return Objects.requireNonNull((InternalHttpMessage) message).unsafeSize();
    private static long unsafeSize(HttpMessage message) {
        HttpMessage actual = Objects.requireNonNull(message);
        return ((InternalHttpMessage) actual).unsafeSize();
    }

    @Benchmark
    public long unsafeTotalSize() {
        return unsafeSize(msg);
    }

    @Benchmark
    @Threads(2)
    public long unsafeTotalSize2() {
        return unsafeSize(msg);
    }

    @Benchmark
    @Threads(3)
    public long unsafeTotalSize3() {
        return unsafeSize(msg);
    }

    @Benchmark
    @Threads(4)
    public long unsafeTotalSize4() {
        return unsafeSize(msg);
    }

    @Benchmark
    @Threads(5)
    public long unsafeTotalSize5() {
        return unsafeSize(msg);
    }

    @Benchmark
    @Threads(6)
    public long unsafeTotalSize6() {
        return unsafeSize(msg);
    }
}
