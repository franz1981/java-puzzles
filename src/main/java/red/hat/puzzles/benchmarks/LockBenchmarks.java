/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.hat.puzzles.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.locks.StampedLock;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
//@Threads(Threads.MAX)
public class LockBenchmarks extends StampedLock {

    private static final String key = "key";
    private int value;
    private HashMap<String, Object> map;
    private Object sync;
    private static final AtomicLongFieldUpdater<LockBenchmarks> VERSION_UPDATER = AtomicLongFieldUpdater.newUpdater(LockBenchmarks.class, "version");
    private volatile long version = 0;
    private AtomicLongArray paddedVersion;

    @Setup
    public void init() {
        map = new HashMap<>();
        map.put(key, key);
        //to avoid Integer boxing pooling
        value = Integer.MAX_VALUE;
        sync = new Object();
        paddedVersion = new AtomicLongArray(32);
    }

    @Benchmark
    public Object readLockGetObject() {
        final long stamp = this.readLock();
        try {
            return map.get(key);
        } finally {
            this.unlockRead(stamp);
        }
    }

    @Benchmark
    public Object writeLockGetObject() {
        final long stamp = this.writeLock();
        try {
            return map.get(key);
        } finally {
            this.unlockWrite(stamp);
        }
    }

    private <T> T read(Supplier<? extends T> supplier) {
        final long stamp = this.readLock();
        try {
            return supplier.get();
        } finally {
            this.unlockRead(stamp);
        }
    }

    private <T> T write(Supplier<? extends T> supplier) {
        final long stamp = this.writeLock();
        try {
            return supplier.get();
        } finally {
            this.unlockWrite(stamp);
        }
    }

    private int primitiveRead(IntSupplier supplier) {
        final long stamp = this.readLock();
        try {
            return supplier.getAsInt();
        } finally {
            this.unlockRead(stamp);
        }
    }

    private int primitiveWrite(IntSupplier supplier) {
        final long stamp = this.readLock();
        try {
            return supplier.getAsInt();
        } finally {
            this.unlockRead(stamp);
        }
    }


    @Benchmark
    public Object readLockGetObjectWithLambda() {
        final String key = this.key;
        //forced to be capturing
        return read(() -> map.get(key));
    }

    @Benchmark
    public Object writeLockGetObjectWithLambda() {
        final String key = this.key;
        //forced to be capturing
        return write(() -> map.get(key));
    }

    @Benchmark
    public int readLockGetIntWithBoxedLambda() {
        return read(() -> value);
    }

    @Benchmark
    public int writeLockGetIntWithBoxedLambda() {
        return write(() -> value);
    }

    @Benchmark
    public int readLockGetIntWithPrimitiveLambda() {
        return primitiveRead(() -> value);
    }

    @Benchmark
    public int writeLockGetIntWithPrimitiveLambda() {
        return primitiveWrite(() -> value);
    }

    @Benchmark
    public int readLockGetInt() {
        final long stamp = this.readLock();
        try {
            return value;
        } finally {
            this.unlockRead(stamp);
        }
    }

    @Benchmark
    public int writeLockGetInt() {
        final long stamp = this.writeLock();
        try {
            return value;
        } finally {
            this.unlockWrite(stamp);
        }
    }

    @Benchmark
    public int syncGetInt() {
        synchronized (sync) {
            return value;
        }
    }

    @Benchmark
    public Object syncGetObject() {
        synchronized (sync) {
            return map.get(key);
        }
    }

    @Benchmark
    public int baselinePrimitive() {
        return value;
    }

    @Benchmark
    public Object baselineGetObject() {
        return map.get(key);
    }

    @Benchmark
    public Object paddedVersionedGetObject() {
        final AtomicLongArray paddedVersion = this.paddedVersion;
        while (paddedVersion.getAndIncrement(16) > 0) {
            Thread.yield();
        }
        try {
            return this.map.get(key);
        } finally {
            paddedVersion.lazySet(16, 0);
        }
    }

    @Benchmark
    public int paddedVersionedGetInt() {
        final AtomicLongArray paddedVersion = this.paddedVersion;
        while (paddedVersion.getAndIncrement(16) > 0) {
            Thread.yield();
        }
        try {
            return this.value;
        } finally {
            paddedVersion.lazySet(16, 0);
        }
    }

    @Benchmark
    public Object versionedGetObject() {
        while (VERSION_UPDATER.getAndIncrement(this) > 0) {
            Thread.yield();
        }
        try {
            return this.map.get(key);
        } finally {
            VERSION_UPDATER.lazySet(this, 0);
        }
    }

    @Benchmark
    public int versionedGetInt() {
        while (VERSION_UPDATER.getAndIncrement(this) > 0) {
            Thread.yield();
        }
        try {
            return this.value;
        } finally {
            VERSION_UPDATER.lazySet(this, 0);
        }
    }

    public static void main(String[] args) throws RunnerException {
        //try it with
        final Options opt = new OptionsBuilder()
                .include(LockBenchmarks.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                //.jvmArgs("-XX:-UseBiasedLocking")
                .build();
        new Runner(opt).run();
    }
}
