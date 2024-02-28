package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import static red.hat.puzzles.polymorphism.Encoder.*;

import java.util.concurrent.TimeUnit;

/**
 * See https://bugs.openjdk.org/browse/JDK-8180450 for more info.
 * This benchmark must run with both -t 1 and -t <available cores>.<br>
 * Suggested profilers are -prof perfnorm perfasm perfc2c.
 * <p>
 * Purpose of the benchmark is not to compare results between benchmarked methods, but
 * study the ASM produced and run the same experiments with different -t values to
 * evaluate the effects on scalability of the mentioned issue.
 * <p>
 * The code emulated is https://github.com/netty/netty/blob/4.1/codec-http/src/main/java/io/netty/handler/codec/http/HttpObjectEncoder.java#L83
 * in the context of the Vert-x usage (see https://github.com/eclipse-vertx/vert.x/blob/09970b9ed4d49ee95722720766f632f89b4a3d09/src/main/java/io/vertx/core/http/impl/Http1xServerResponse.java#L411)
 * while running Quarkus https://github.com/TechEmpower/FrameworkBenchmarks.
 */
@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
public class InstanceOfScalabilityBenchmark {

    /**
     * Please run: red.hat.puzzles.polymorphism.InstanceOfScalabilityBenchmark.encodeFullType -pencoderType=a -ppollutionCases=20000
     *
     * with -t 1 and -t N (where N > 1) and compare the results.
     */
    static class AssembledHttpResponse implements HttpResponse, HttpContent {

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public void release() {

        }
    }

    static class AssembledFullHttpResponse extends AssembledHttpResponse implements FullHttpResponse {

    }

    static class DefaultHttpResponse implements HttpResponse, HttpContent {

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public void release() {

        }
    }

    static class DefaultFullHttpResponse extends DefaultHttpResponse implements FullHttpResponse {

    }

    private Object assembledHttpResponse;
    private Object assembledFullHttpResponse;

    /**
     * Type a is using the HttpObjectEncoderA and type b is using the HttpObjectEncoderB.
     *
     * The former is using the original code from Netty, which perform different type checks to encode the object,
     * while the latter try to short-circuit the checks for FullHttpMessage (and others) by checking first mixin marker
     * interfaces.
     *
     * The latter is supposed to better scale than the former.
     */
    @Param({"a", "b"})
    private String encoderType;

    /**
     * This parameter is going to further pollute the type profile of the instanceof checks by artificially using
     * the already existing 2 concrete types, extending them to use 4 different types.
     */
    @Param({"false", "true"})
    private boolean polluteExistingTypes;

    @Param({"0", "20000"})
    private int pollutionCases;

    private Encoder encoder;

    @Setup
    public void init() {
        switch (encoderType) {
            case "a":
                encoder = new HttpObjectEncoderA<HttpResponse>();
                break;
            case "b":
                encoder = new HttpObjectEncoderB<HttpResponse>();
                break;
            default:
                throw new AssertionError("not supported encoder");
        }
        if (pollutionCases > 0) {
            // it is making the non-used default types twice as more frequent than the used ones
            Object[] types = polluteExistingTypes ?
                    new Object[]{
                            new AssembledHttpResponse(),
                            new AssembledFullHttpResponse(),
                            new DefaultHttpResponse(),
                            new DefaultFullHttpResponse(),
                            new DefaultHttpResponse(),
                            new DefaultFullHttpResponse()
                    } :
                    new Object[]{new AssembledHttpResponse(), new AssembledFullHttpResponse()};
            for (int i = 0; i < pollutionCases; i++) {
                for (Object type : types) {
                    encoder.encode(type);
                }
            }
        } else {
            if (polluteExistingTypes) {
                // kill this fork
                System.exit(0);
            }
        }
        assembledHttpResponse = new AssembledHttpResponse();
        assembledFullHttpResponse = new AssembledFullHttpResponse();
    }

    @Benchmark
    public void encodeFullType() {
        encoder.encode(assembledFullHttpResponse);
    }

    @Benchmark
    public void encodeMixType() {
        encoder.encode(assembledHttpResponse);
    }

}
