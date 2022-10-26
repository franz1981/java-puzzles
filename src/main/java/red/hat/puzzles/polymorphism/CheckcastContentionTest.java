package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 15, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class CheckcastContentionTest {

    /**
     * Vert-x internal API isn't supposed to be exposed outside the framework, and can
     * implements additional internal "unsafe" methods.
     */
    public interface VertxHttpMessage extends HttpMessage {
        long unsafeSize();
    }

    public interface HttpMessage {
        long safeSize();
    }

    public static class HttpStatefullMessage implements VertxHttpMessage {

        private long randomField = 1;

        @Override
        public long unsafeSize() {
            return randomField;
        }

        @Override
        public long safeSize() {
            return randomField;
        }
    }

    public static class HttpStatelessMessage implements VertxHttpMessage {

        private long randomField = 2;

        @Override
        public long unsafeSize() {
            return randomField;
        }

        @Override
        public long safeSize() {
            return randomField;
        }
    }

    private List<VertxHttpMessage> vertxHttpMessage;
    private List<HttpMessage> httpMessage;

    @Param({"false", "true"})
    private boolean typePollution;

    @Setup
    public void init() {
        vertxHttpMessage = new ArrayList<>(2);
        httpMessage = new ArrayList<>(2);
        vertxHttpMessage.add(new HttpStatelessMessage());
        if (typePollution) {
            vertxHttpMessage.add(new HttpStatefullMessage());
        } else {
            vertxHttpMessage.add(new HttpStatelessMessage());
        }
        httpMessage.add(new HttpStatelessMessage());
        if (typePollution) {
            httpMessage.add(new HttpStatefullMessage());
        } else {
            httpMessage.add(new HttpStatelessMessage());
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public long unsafeTotalSize() {
        long size = 0;
        for (VertxHttpMessage i : vertxHttpMessage) {
            size += i.unsafeSize();
        }
        return size;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public long safeTotalSize() {
        long size = 0;
        for (HttpMessage i : httpMessage) {
            size += i.safeSize();
        }
        return size;
    }

}
