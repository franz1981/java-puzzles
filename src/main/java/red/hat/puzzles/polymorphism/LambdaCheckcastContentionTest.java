package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 15, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class LambdaCheckcastContentionTest {

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

    private InternalHttpMessage[] internalHttpMessages;
    private HttpMessage[] httpMessages;

    @Param({"false", "true"})
    private boolean typePollution;

    @Setup
    public void init() {
        internalHttpMessages = new InternalHttpMessage[2];
        internalHttpMessages[0] = new HttpStatelessMessage();
        if (typePollution) {
            internalHttpMessages[1] = new HttpStatefullMessage();
        } else {
            internalHttpMessages[1] = new HttpStatelessMessage();
        }
        httpMessages = new HttpMessage[2];
        httpMessages[0] = new HttpStatelessMessage();
        if (typePollution) {
            httpMessages[1] = new HttpStatefullMessage();
        } else {
            httpMessages[1] = new HttpStatelessMessage();
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static <T> long genericUnsafeForEach(InternalHttpMessage[] msgs, ToLongFunction<InternalHttpMessage> f) {
        long v = 0;
        for (InternalHttpMessage msg : msgs) {
            v += f.applyAsLong(msg);
        }
        return v;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static <T> long genericSafeForEach(HttpMessage[] msgs, ToLongFunction<HttpMessage> f) {
        long v = 0;
        for (HttpMessage msg : msgs) {
            v += f.applyAsLong(msg);
        }
        return v;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("generic")
    public long genericUnsafeTotalSize() {
        return genericUnsafeForEach(internalHttpMessages, InternalHttpMessage::unsafeSize)
                + genericUnsafeForEach(internalHttpMessages, msg -> msg.unsafeSize());
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("generic")
    public long genericSafeTotalSize() {
        return genericSafeForEach(httpMessages, HttpMessage::safeSize) +
                genericSafeForEach(httpMessages, msg -> msg.safeSize());

    }

    @FunctionalInterface
    private interface InternalHttpMessageToLong {
        long applyAsLong(InternalHttpMessage msg);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static <T> long reifiedUnsafeForEach(InternalHttpMessage[] msgs, InternalHttpMessageToLong f) {
        long v = 0;
        for (InternalHttpMessage msg : msgs) {
            v += f.applyAsLong(msg);
        }
        return v;
    }

    @FunctionalInterface
    private interface HttpMessageToLong {
        long applyAsLong(HttpMessage msg);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static <T> long reifiedSafeForEach(HttpMessage[] msgs, HttpMessageToLong f) {
        long v = 0;
        for (HttpMessage msg : msgs) {
            v += f.applyAsLong(msg);
        }
        return v;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("reified")
    public long reifiedUnsafeTotalSize() {
        return reifiedUnsafeForEach(internalHttpMessages, InternalHttpMessage::unsafeSize)
                + reifiedUnsafeForEach(internalHttpMessages, msg -> msg.unsafeSize());
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    @Group("reified")
    public long reifiedSafeTotalSize() {
        return reifiedSafeForEach(httpMessages, HttpMessage::safeSize) +
                reifiedSafeForEach(httpMessages, msg -> msg.safeSize());

    }

}
