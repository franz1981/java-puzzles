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
 * <p>
 * TODO:
 * - [EASY] add some BlackHole::consumeCPU to emulate some works
 * - [EASY] experiments polluting the type profiles of instanceof checks in the warmup phase
 * (see more info on https://wiki.openjdk.org/display/HotSpot/MethodData) *
 * - [HARD] experiments inlining effects (if any)
 */
@State(Scope.Thread)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class InstanceOfScalabilityBenchmark {

    static class AssembledHttpResponse implements HttpResponse, HttpContent {

        @Override
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public void release() {
            // NOP but still complex enough to not been inlined
        }
    }

    static class AssembledFullHttpResponse extends AssembledHttpResponse implements FullHttpResponse {

    }

    private Object assembledHttpResponse;
    private Object assembledFullHttpResponse;

    @Param({"a", "b"})
    private String encoderType;

    @Param({"0", "20000"})
    private int typePollution;

    private Encoder encoder;

    @Setup
    public void init() {
        assembledHttpResponse = new AssembledHttpResponse();
        assembledFullHttpResponse = new AssembledFullHttpResponse();
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
        // using all types would poison the type profile:
        // it's still a bit noisy because code shape depends on frequencies of types
        for (int i = 0; i < typePollution; i++) {
            encodeAllTypes();
        }
    }

    @Benchmark
    public void encodeAllTypes() {
        encoder.encode(assembledFullHttpResponse);
        encoder.encode(assembledHttpResponse);
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
