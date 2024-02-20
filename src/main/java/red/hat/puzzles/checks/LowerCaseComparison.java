package red.hat.puzzles.checks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LowerCaseComparison {

    @Param({"false", "true"})
    public boolean same;
    private AsciiString asciiStrings;
    private AsciiString otherAscii;

    @Setup
    public void init() {
        asciiStrings = new AsciiString("Connection");
        otherAscii = new AsciiString("connection");
    }

    @Benchmark
    public boolean unoptimizedContentEqualsIgnoreCase() {
        return asciiStrings.unoptimizedContentEqualsIgnoreCase(otherAscii);
    }

    @Benchmark
    public boolean optimizedContentEqualsIgnoreCase() {
        return asciiStrings.optimizedContentEqualsIgnoreCase(otherAscii);
    }

}
