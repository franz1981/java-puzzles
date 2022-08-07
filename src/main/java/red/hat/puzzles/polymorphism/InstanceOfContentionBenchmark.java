package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * See https://bugs.openjdk.org/browse/JDK-8180450 for more info.
 * This benchmark must run with both -t 1 and -t <available cores>.<br>
 * Suggested profilers are -prof perfnorm -prof perfasm -prof perfc2c
 */
@State(Scope.Benchmark)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class InstanceOfContentionBenchmark {
    interface HttpMessage {
    }

    interface HttpResponse extends HttpMessage {
    }

    interface HttpContent {
    }

    interface HttpLastContent extends HttpContent {
    }

    class AssembledHttpResponse implements HttpResponse, HttpContent {

    }

    class AssembledFullHttpResponse implements HttpResponse, HttpLastContent {

    }

    class AssembledLastHttpContent implements HttpLastContent {

    }


    private Object assembledHttpResponse;
    private Object assembledFullHttpResponse;
    private Object assembledLastHttpContent;


    @Setup
    public void init() {
        assembledHttpResponse = new AssembledHttpResponse();
        assembledFullHttpResponse = new AssembledFullHttpResponse();
        assembledLastHttpContent = new AssembledLastHttpContent();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static int encode(Object o, Blackhole bh) {
        int result = 0;
        if (o instanceof HttpMessage) {
            HttpResponse response = (HttpResponse) o;
            bh.consume(response);
            result++;
            bh.consume(result);
        }
        if (o instanceof HttpContent) {
            result++;
            bh.consume(result);
            if (o instanceof HttpLastContent) {
                result++;
                bh.consume(result);
            }
        }
        return result;
    }

    @Benchmark
    public int checkAllTypes(Blackhole bh) {
        return encode(assembledFullHttpResponse, bh) + encode(assembledHttpResponse, bh) + encode(assembledLastHttpContent, bh);
    }

    @Benchmark
    public int checkTwoTypes(Blackhole bh) {
        return encode(assembledHttpResponse, bh) + encode(assembledLastHttpContent, bh) + encode(assembledLastHttpContent, bh);
    }

    @Benchmark
    public int checkFull(Blackhole bh) {
        return encode(assembledFullHttpResponse, bh);
    }


}
