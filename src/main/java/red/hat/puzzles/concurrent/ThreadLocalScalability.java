package red.hat.puzzles.concurrent;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@Fork(2)
@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 10, time = 1)
public class ThreadLocalScalability {

    private static final int COUNT = Integer.getInteger("threadLocals", 8);
    @SuppressWarnings("unchecked")
    private static final ThreadLocal<Boolean>[] THREAD_LOCALS = new ThreadLocal[COUNT];

    static {
        for (int i = 0; i < THREAD_LOCALS.length; i ++) {
            THREAD_LOCALS[i] = new ThreadLocal<>();
        }
    }

    @Benchmark
    public void setGetRemove(Blackhole bh) {
        for (var tl : THREAD_LOCALS) {
            tl.set(Boolean.TRUE);
            bh.consume(tl.get());
            tl.remove();
        }
    }

    @Benchmark
    public void setGetNullout(Blackhole bh) {
        for (var tl : THREAD_LOCALS) {
            tl.set(Boolean.TRUE);
            bh.consume(tl.get());
            tl.set(null);
        }
    }

}
