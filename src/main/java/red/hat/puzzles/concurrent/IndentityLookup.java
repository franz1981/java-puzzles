package red.hat.puzzles.concurrent;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 2)
public class IndentityLookup {

    private SingleWriterCopyOnWriteArrayIdentityMap<ClassLoader, Object> arrayMap;
    private Map<ClassLoader, Object> hashMap;
    @Param({"1"})
    public int size;
    @Param({"true"})
    public boolean disableHashCodeIntrinsics;

    private ClassLoader missingClassLoader;
    private ClassLoader firstClassLoader;
    private ClassLoader lastClassLoader;
    private CountDownLatch unblock;

    private ExecutorService executor;

    private static final class CustomClassLoader extends ClassLoader {
    }

    @Setup
    public void init(Blackhole bh) {
        executor = Executors.newCachedThreadPool();
        unblock = new CountDownLatch(1);
        arrayMap = new SingleWriterCopyOnWriteArrayIdentityMap<>();
        hashMap = new HashMap<>();
        // create N different instances of ClassLoader
        missingClassLoader = new CustomClassLoader();
        for (int i = 0; i < size; i++) {
            var classLoader = new CustomClassLoader();
            if (i == 0) {
                firstClassLoader = classLoader;
                if (disableHashCodeIntrinsics) {
                    executor.execute(() -> {
                        synchronized (firstClassLoader) {
                            try {
                                unblock.await();
                            } catch (Throwable ignore) {

                            }
                        }
                    });
                }
            }
            if (i == size - 1) {
                lastClassLoader = classLoader;
                if (disableHashCodeIntrinsics && lastClassLoader != firstClassLoader) {
                    executor.execute(() -> {
                        synchronized (lastClassLoader) {
                            try {
                                unblock.await();
                            } catch (Throwable ignore) {

                            }
                        }
                    });
                }
            }
            arrayMap.put(classLoader, new Object());
            hashMap.put(classLoader, new Object());
            if (disableHashCodeIntrinsics) {
                executor.execute(() -> {
                    synchronized (missingClassLoader) {
                        try {
                            unblock.await();
                        } catch (Throwable ignore) {

                        }
                    }
                });
                executor.execute(() -> {
                    synchronized (firstClassLoader) {
                        try {
                            unblock.await();
                        } catch (Throwable ignore) {

                        }
                    }
                });
                if (lastClassLoader != null) {
                    executor.execute(() -> {
                        synchronized (lastClassLoader) {
                            try {
                                unblock.await();
                            } catch (Throwable ignore) {

                            }
                        }
                    });
                }
            }
        }
    }

    @Benchmark
    public Object arrayMapGetMissing() {
        return arrayMap.get(missingClassLoader);
    }

    @Benchmark
    public Object hashMapGetMissing() {
        return hashMap.get(missingClassLoader);
    }

    @Benchmark
    public Object arrayMapGetFirst() {
        return arrayMap.get(firstClassLoader);
    }

    @Benchmark
    public Object hashMapGetFirst() {
        return hashMap.get(firstClassLoader);
    }

    @Benchmark
    public Object arrayMapGetLast() {
        return arrayMap.get(lastClassLoader);
    }

    @Benchmark
    public Object hashMapGetLast() {
        return hashMap.get(lastClassLoader);
    }

    @Benchmark
    public Object classLoaderHashCode() {
        return missingClassLoader.hashCode();
    }

    @TearDown
    public void tearDown() throws InterruptedException {
        unblock.countDown();
        executor.shutdownNow();
        // await till all tasks completes
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }


}
