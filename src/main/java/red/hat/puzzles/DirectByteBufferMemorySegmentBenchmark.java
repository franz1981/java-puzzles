package red.hat.puzzles;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
public class DirectByteBufferMemorySegmentBenchmark {

    // Inverted layout: LONG first (8 bytes) at offset 0, INT after it (4 bytes at offset 8)
    private static final int LONG_OFFSET = 0;
    private static final int INT_OFFSET = LONG_OFFSET + Long.BYTES; // 8
    private static final int SIZE_BYTES = INT_OFFSET + Integer.BYTES; // 12

    private ByteBuffer directBuffer;
    private static final VarHandle INT_HANDLE = MethodHandles.byteBufferViewVarHandle(int[].class, ByteOrder.nativeOrder());
    private static final VarHandle LONG_HANDLE = MethodHandles.byteBufferViewVarHandle(long[].class, ByteOrder.nativeOrder());

    private MemorySegment memorySegment;


    @Setup(Level.Trial)
    public void setup() throws Exception {
        memorySegment = Arena.global().allocate(SIZE_BYTES);
        directBuffer = ByteBuffer.allocateDirect(SIZE_BYTES).order(ByteOrder.nativeOrder());
        // populate with random data
        byte[] data = new byte[SIZE_BYTES];
        new Random(42).nextBytes(data);
        directBuffer.position(0);
        // put entire array into the buffer at position 0
        directBuffer.put(data);
    }

    // Contract: each benchmark reads a long, an int, and consumes them.

    @Benchmark
    public void byteBufferAbsoluteAccess(Blackhole bh) {
        // read long first then int (now contiguous with LONG first)
        long l = directBuffer.getLong(LONG_OFFSET);
        int i = directBuffer.getInt(INT_OFFSET);
        bh.consume(l);
        bh.consume(i);
    }

    @Benchmark
    public void byteBufferVarHandleAccess(Blackhole bh) {
        // VarHandle view indices are in units of the view element size
        long l = (long) LONG_HANDLE.get(directBuffer, LONG_OFFSET);
        int i = (int) INT_HANDLE.get(directBuffer, INT_OFFSET);
        bh.consume(l);
        bh.consume(i);
    }

    @Benchmark
    public void memorySegmentAccess(Blackhole bh) {
        long l = memorySegment.get(ValueLayout.JAVA_LONG, LONG_OFFSET);
        int i = memorySegment.get(ValueLayout.JAVA_INT, INT_OFFSET);
        bh.consume(l);
        bh.consume(i);
    }

    @Benchmark
    public void memorySegmentUnalignedAccess(Blackhole bh) {
        long l = memorySegment.get(ValueLayout.JAVA_LONG_UNALIGNED, LONG_OFFSET);
        int i = memorySegment.get(ValueLayout.JAVA_INT_UNALIGNED, INT_OFFSET);
        bh.consume(l);
        bh.consume(i);
    }
}
