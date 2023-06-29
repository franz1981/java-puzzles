package red.hat.puzzles.concurrent;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class DoubleCheckedLockingInitial {
    private static class SynchronizedLazySetSingleton {
        private static final AtomicReferenceFieldUpdater<SynchronizedLazySetSingleton, ConcurrentMap> MAP_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(SynchronizedLazySetSingleton.class, ConcurrentMap.class, "map");

        private volatile ConcurrentMap<Object, Object> map;

        public ConcurrentMap<Object, Object> map() {
            var map = this.map;
            if (map != null) {
                return map;
            }
            synchronized (this) {
                map = this.map;
                if (map != null) {
                    return map;
                }
                map = new ConcurrentHashMap<>();
                MAP_UPDATER.lazySet(this, map);
                return map;
            }
        }
    }

    private static class CompareAndSetSingleton {

        private static final AtomicReferenceFieldUpdater<CompareAndSetSingleton, ConcurrentMap> MAP_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(CompareAndSetSingleton.class, ConcurrentMap.class, "map");

        private volatile ConcurrentMap<Object, Object> map;

        public ConcurrentMap<Object, Object> map() {
            var map = this.map;
            if (map != null) {
                return map;
            }
            ConcurrentMap<Object, Object> newMap = new ConcurrentHashMap<>();
            if (!MAP_UPDATER.compareAndSet(this, null, newMap)) {
                return this.map;
            }
            return newMap;
        }
    }

    private static class SynchronizedVolatileSetSingleton {
        private volatile ConcurrentMap<Object, Object> map;

        public ConcurrentMap<Object, Object> map() {
            var map = this.map;
            if (map != null) {
                return map;
            }
            synchronized (this) {
                map = this.map;
                if (map != null) {
                    return map;
                }
                map = new ConcurrentHashMap<>();
                this.map = map;
                return map;
            }
        }
    }

    @Param({"0", "100"})
    public int work;

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object synchronizedVolatileSet(Blackhole bh) {
        var singleton = new SynchronizedVolatileSetSingleton();
        var map = singleton.map();
        bh.consume(singleton);
        if (work > 0) {
            Blackhole.consumeCPU(work);
        }
        return map;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object synchronizedLazySet(Blackhole bh) {
        var singleton = new SynchronizedLazySetSingleton();
        var map = singleton.map();
        bh.consume(singleton);
        if (work > 0) {
            Blackhole.consumeCPU(work);
        }
        return map;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object compareAndSetSingleton(Blackhole bh) {
        var singleton = new CompareAndSetSingleton();
        var map = singleton.map();
        bh.consume(singleton);
        if (work > 0) {
            Blackhole.consumeCPU(work);
        }
        return map;
    }
}
