package red.hat.puzzles.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class UnrolledLinkedListBench {

    private static final Long V = 0L;

    List<Long> list;
    @Param({"1", "64", "128", "1024"})
    int size;
    @Param({"linked", "array", "unrolled"})
    String type;
    int index;
    long sum = 0;
    Consumer<Long> consumer;

    @Setup
    public void init() {
        index = size / 2;
        switch (type) {
            case "linked":
                list = new LinkedList<>();
                break;
            case "array":
                list = new ArrayList<>(size);
                break;
            case "unrolled":
                list = new UnrolledLinkedList<>();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        for (int i = 0; i < size; i++) {
            list.add(V);
        }
        consumer = v -> sum+=v;
    }

    @Benchmark
    public long forEach(){
        sum = 0;
        list.forEach(consumer);
        return sum;
    }

    @Benchmark
    public void replaceInTheMiddle(Blackhole bh){
        list.remove(index);
        bh.consume(list);
        list.add(index, V);
    }


}
