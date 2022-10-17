package red.hat.puzzles;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class AsciiCopy {

    private byte[] asciiContent;
    private char[] asciiContentChars;

    @Param({"0", "1"})
    private int start;

    @Param({"8", "16"})
    private int end;

    @Setup
    public void setup() {
        asciiContent = new byte[end];
        asciiContentChars = new char[end];
        for (int i = 0; i < end; i++) {
            asciiContent[i] = 'a';
            asciiContentChars[i] = 'a';
        }
    }

    @Benchmark
    public byte[] bytesCopy() {
        final byte[] asciiContent = this.asciiContent;
        final int start = this.start;
        final int end = this.end;
        final int newLen = end - start;
        final byte[] copy = new byte[newLen];
        System.arraycopy(asciiContent, start, copy, 0, copy.length);
        return copy;
    }

    @Benchmark
    public byte[] bytesOptimizedCopyOfRange() {
        final byte[] asciiContent = this.asciiContent;
        final int start = this.start;
        final int end = this.end;
        if (start == 0) {
            if (end == asciiContent.length) {
                return Arrays.copyOfRange(asciiContent, 0, asciiContent.length);
            }
            return Arrays.copyOfRange(asciiContent, 0, end);
        }
        return Arrays.copyOfRange(asciiContent, start, end);
    }

    @Benchmark
    public byte[] bytesCopyOfRange() {
        final byte[] asciiContent = this.asciiContent;
        final int start = this.start;
        final int end = this.end;
        return Arrays.copyOfRange(asciiContent, start, end);
    }

    @Benchmark
    public String asciiStringBytesCopy() {
        final byte[] asciiContent = this.asciiContent;
        final int start = this.start;
        final int end = this.end;
        final int newLen = end - start;
        return new String(asciiContent, 0, start, newLen);
    }

    @Benchmark
    public String asciiStringOptimizedBytesCopy() {
        final byte[] asciiContent = this.asciiContent;
        final int start = this.start;
        final int end = this.end;
        if (start == 0) {
            if (end == asciiContent.length) {
                return new String(asciiContent, 0, 0, asciiContent.length);
            }
            return new String(asciiContent, 0, 0, end);
        }
        final int newLen = end - start;
        return new String(asciiContent, 0, start, newLen);
    }

    @Benchmark
    public String asciiStringCharCopy() {
        final char[] asciiContent = this.asciiContentChars;
        final int start = this.start;
        final int end = this.end;
        final int newLen = end - start;
        return new String(asciiContent, start, newLen);
    }


}
