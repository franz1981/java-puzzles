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

    public interface InternalHttpMessage extends HttpMessage {
        long unsafeSize();
    }

    public interface HttpMessage {
        long safeSize();
    }

    public static class HttpStatefullMessage implements InternalHttpMessage {

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

    public static class HttpStatelessMessage implements InternalHttpMessage {

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

    private List<InternalHttpMessage> internalHttpMessages;
    private List<HttpMessage> httpMessages;

    @Param({"false", "true"})
    private boolean typePollution;

    @Setup
    public void init() {
        internalHttpMessages = new ArrayList<>(2);
        internalHttpMessages.add(new HttpStatelessMessage());
        if (typePollution) {
            internalHttpMessages.add(new HttpStatefullMessage());
        } else {
            internalHttpMessages.add(new HttpStatelessMessage());
        }
        httpMessages = new ArrayList<>(2);
        httpMessages.add(new HttpStatelessMessage());
        if (typePollution) {
            httpMessages.add(new HttpStatefullMessage());
        } else {
            httpMessages.add(new HttpStatelessMessage());
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public long unsafeTotalSize() {
        long size = 0;
        for (InternalHttpMessage i : internalHttpMessages) {
            size += i.unsafeSize();
        }
        return size;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("normal")
    public long safeTotalSize() {
        long size = 0;
        for (HttpMessage i : httpMessages) {
            size += i.safeSize();
        }
        return size;
    }

}
