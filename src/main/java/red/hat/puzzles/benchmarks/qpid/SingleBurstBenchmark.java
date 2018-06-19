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

package red.hat.puzzles.benchmarks.qpid;

import io.netty.util.concurrent.DefaultEventExecutor;
import org.apache.qpid.jms.provider.BalancedProviderFuture;
import org.apache.qpid.jms.provider.ConservativeProviderFuture;
import org.apache.qpid.jms.provider.ProgressiveProviderFuture;
import org.apache.qpid.jms.provider.ProviderFuture;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SingleBurstBenchmark {

    @Param({"spinning", "balanced", "conservative", "progressive"})
    private String providerFutureType;
    @Param({"jctools-spin", "netty", "jctools-lock", "jctools-park", "scheduled", "fj"})
    private String executorType;

    private Supplier<? extends ProviderFuture> futureFactory;
    private ExecutorService executorService;

    @Setup
    public void init() {
        switch (providerFutureType) {
            case "spinning":
                futureFactory = SpinningProviderFuture::new;
                break;
            case "balanced":
                futureFactory = BalancedProviderFuture::new;
                break;
            case "conservative":
                futureFactory = ConservativeProviderFuture::new;
                break;
            case "progressive":
                futureFactory = ProgressiveProviderFuture::new;
                break;
        }
        switch (executorType) {
            case "netty":
                executorService = new DefaultEventExecutor();
                break;
            case "scheduled":
                executorService = new ScheduledThreadPoolExecutor(1);
                break;
            case "fj":
                executorService = new ForkJoinPool(1);
                break;
            case "jctools-lock":
                executorService = new ExecutorService() {

                    private final Queue<Runnable> tasks = new MpscArrayQueue<>(64);
                    private final ReentrantLock lock = new ReentrantLock();
                    private final Condition condition = lock.newCondition();
                    private volatile boolean running = true;
                    private final Thread executorThread = new Thread(() -> {
                        do {
                            running = true;
                            Runnable task;
                            while ((task = tasks.poll()) != null) {
                                try {
                                    task.run();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                            running = false;
                            if (!tasks.isEmpty()) {
                                continue;
                            }
                            lock.lock();
                            try {
                                if (tasks.isEmpty()) {
                                    try {
                                        condition.await();
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        //NOOP
                                    }
                                }
                            } finally {
                                lock.unlock();
                            }
                        } while (!Thread.currentThread().isInterrupted());
                        //simple eh? :P
                        tasks.clear();
                    });

                    {
                        executorThread.start();
                    }

                    @Override
                    public void shutdown() {
                        executorThread.interrupt();
                        try {
                            executorThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public List<Runnable> shutdownNow() {
                        return null;
                    }

                    @Override
                    public boolean isShutdown() {
                        return false;
                    }

                    @Override
                    public boolean isTerminated() {
                        return false;
                    }

                    @Override
                    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                        return false;
                    }

                    @Override
                    public <T> Future<T> submit(Callable<T> task) {
                        return null;
                    }

                    @Override
                    public <T> Future<T> submit(Runnable task, T result) {
                        return null;
                    }

                    @Override
                    public Future<?> submit(Runnable task) {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        return null;
                    }

                    @Override
                    public void execute(Runnable command) {
                        if (!tasks.offer(command)) {
                            throw new RejectedExecutionException("back-pressured?");
                        }
                        if (!running) {
                            lock.lock();
                            try {
                                condition.signal();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                };
                break;
            case "jctools-spin":
                executorService = new ExecutorService() {

                    private final MessagePassingQueue<Runnable> tasks = new MpscArrayQueue<>(64);
                    private final Thread executorThread = new Thread(() -> {
                        final Thread currentThread = Thread.currentThread();
                        do {
                            Runnable task;
                            while ((task = tasks.poll()) != null) {
                                task.run();
                            }
                        } while (!currentThread.isInterrupted());
                        //simple eh? :P
                        tasks.clear();
                    });

                    {
                        executorThread.start();
                    }

                    @Override
                    public void shutdown() {
                        executorThread.interrupt();
                        try {
                            executorThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public List<Runnable> shutdownNow() {
                        return null;
                    }

                    @Override
                    public boolean isShutdown() {
                        return false;
                    }

                    @Override
                    public boolean isTerminated() {
                        return false;
                    }

                    @Override
                    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                        return false;
                    }

                    @Override
                    public <T> Future<T> submit(Callable<T> task) {
                        return null;
                    }

                    @Override
                    public <T> Future<T> submit(Runnable task, T result) {
                        return null;
                    }

                    @Override
                    public Future<?> submit(Runnable task) {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        return null;
                    }

                    @Override
                    public void execute(Runnable command) {
                        if (!tasks.offer(command)) {
                            throw new RejectedExecutionException("back-pressured?");
                        }
                    }
                };
                break;
            case "jctools-park":
                executorService = new ExecutorService() {

                    private final Queue<Runnable> tasks = new MpscArrayQueue<>(64);
                    private final AtomicReference<Thread> parkedThread = new AtomicReference<>();
                    private final Thread executorThread = new Thread(() -> {
                        final Thread currentThread = Thread.currentThread();
                        do {
                            parkedThread.set(null);
                            Runnable task;
                            while ((task = tasks.poll()) != null) {
                                try {
                                    task.run();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                            parkedThread.set(currentThread);
                            if (tasks.isEmpty()) {
                                LockSupport.park();
                            }
                        } while (!currentThread.isInterrupted());
                        parkedThread.set(null);
                        //simple eh? :P
                        tasks.clear();
                    });

                    {
                        executorThread.start();
                    }

                    @Override
                    public void shutdown() {
                        executorThread.interrupt();
                        try {
                            executorThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public List<Runnable> shutdownNow() {
                        return null;
                    }

                    @Override
                    public boolean isShutdown() {
                        return false;
                    }

                    @Override
                    public boolean isTerminated() {
                        return false;
                    }

                    @Override
                    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                        return false;
                    }

                    @Override
                    public <T> Future<T> submit(Callable<T> task) {
                        return null;
                    }

                    @Override
                    public <T> Future<T> submit(Runnable task, T result) {
                        return null;
                    }

                    @Override
                    public Future<?> submit(Runnable task) {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                        return null;
                    }

                    @Override
                    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        return null;
                    }

                    @Override
                    public void execute(Runnable command) {
                        if (!tasks.offer(command)) {
                            throw new RejectedExecutionException("back-pressured?");
                        }
                        final Thread parked = parkedThread.get();
                        if (parked != null) {
                            LockSupport.unpark(parked);
                        }
                    }
                };
                break;
        }
    }

    @Benchmark
    public void singleBurst() throws IOException, InterruptedException {
        final ProviderFuture providerFuture = futureFactory.get();
        executorService.execute(providerFuture::onSuccess);
        providerFuture.sync();
    }


    @TearDown(Level.Trial)
    public void tearDown() {
        executorService.shutdown();
    }


    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SingleBurstBenchmark.class.getSimpleName())
                .jvmArgs("-XX:-UseBiasedLocking")
                .shouldDoGC(true)
                .build();
        new Runner(opt).run();
    }
}
