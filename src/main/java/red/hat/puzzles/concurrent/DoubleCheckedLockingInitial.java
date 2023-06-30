package red.hat.puzzles.concurrent;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class DoubleCheckedLockingInitial {

    private static class SynchronizedLazySetSingleton implements Supplier<ConcurrentMap<Object, Object>> {
        private static final AtomicReferenceFieldUpdater<SynchronizedLazySetSingleton, ConcurrentMap> MAP_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(SynchronizedLazySetSingleton.class, ConcurrentMap.class, "map");

        private volatile ConcurrentMap<Object, Object> map;

        @Override
        public ConcurrentMap<Object, Object> get() {
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

    private static class CompareAndSetSingleton implements Supplier<ConcurrentMap<Object, Object>> {

        private static final AtomicReferenceFieldUpdater<CompareAndSetSingleton, ConcurrentMap> MAP_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(CompareAndSetSingleton.class, ConcurrentMap.class, "map");

        private volatile ConcurrentMap<Object, Object> map;

        @Override
        public ConcurrentMap<Object, Object> get() {
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

    private static class SynchronizedVolatileSetSingleton implements Supplier<ConcurrentMap<Object, Object>> {
        private volatile ConcurrentMap<Object, Object> map;

        @Override
        public ConcurrentMap<Object, Object> get() {
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

    @Param({"0", "20"})
    public int work;

    @Param({"synchronizedVolatileSet", "synchronizedLazySet", "compareAndSet"})
    public String type;

    private Supplier<Supplier<ConcurrentMap<Object, Object>>> singletonFactory;

    private static final Object KEY = new Object();

    @Setup
    public void init() {
        switch (type) {
            case "synchronizedVolatileSet":
                singletonFactory = SynchronizedVolatileSetSingleton::new;
                break;
            case "synchronizedLazySet":
                singletonFactory = SynchronizedLazySetSingleton::new;
                break;
            case "compareAndSet":
                singletonFactory = CompareAndSetSingleton::new;
                break;
            default:
                throw new UnsupportedOperationException("not supported type = " + type);
        }
    }

    private static Object createValue() {
        return new ConcurrentHashMap<>();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object get(Blackhole bh) {
        var singleton = singletonFactory.get();
        var map = singleton.get();
        bh.consume(singleton);
        if (work > 0) {
            Blackhole.consumeCPU(work);
        }
        return map;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object getAndPut(Blackhole bh) {
        var singleton = singletonFactory.get();
        var map = singleton.get();
        bh.consume(singleton);
        map.put(KEY, createValue());
        if (work > 0) {
            Blackhole.consumeCPU(work);
        }
        return map;
    }
}
