package red.hat.puzzles.loom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class VirtualThreadStartStopCost {

    ThreadFactory factory;
    Runnable task;

    @Setup
    public void init(Blackhole bh) {
        LoomSupport.checkSupported();
        // i.e. starting a v thread will make it run inlined in the benchmarking method
        factory = LoomSupport.setVirtualThreadFactoryScheduler(Thread.ofVirtual(), (Executor) command -> command.run()).factory();
        task = () -> bh.consume(this);
    }


    @Benchmark
    @Fork(value = 2, jvmArgsAppend = "--add-opens=java.base/java.lang=ALL-UNNAMED")
    public Thread startStop() {
        var vThread = factory.newThread(task);
        vThread.start();
        return vThread;
    }

    @Benchmark
    @Fork(value = 2, jvmArgsAppend = {"--add-opens=java.base/java.lang=ALL-UNNAMED", "-Djdk.trackAllThreads=false"})
    public Thread startStopNoContainer() {
        var vThread = factory.newThread(task);
        vThread.start();
        return vThread;
    }

    @Benchmark
    @Fork(value = 2, jvmArgsAppend = {"--add-opens=java.base/java.lang=ALL-UNNAMED", "-XX:+UnlockExperimentalVMOptions", "-XX:-DoJVMTIVirtualThreadTransitions"})
    public Thread startStopNoJvmtiTransitions() {
        var vThread = factory.newThread(task);
        vThread.start();
        return vThread;
    }

    @Benchmark
    @Fork(value = 2, jvmArgsAppend = {"--add-opens=java.base/java.lang=ALL-UNNAMED", "-XX:+UnlockExperimentalVMOptions", "-XX:-DoJVMTIVirtualThreadTransitions", "-Djdk.trackAllThreads=false"})
    public Thread startStopNoJvmtiTransitionsNoContainer() {
        var vThread = factory.newThread(task);
        vThread.start();
        return vThread;
    }

}
