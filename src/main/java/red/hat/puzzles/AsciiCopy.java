package red.hat.puzzles;

import org.openjdk.jmh.annotations.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class AsciiCopy {

    private byte[] asciiContent;
    private char[] asciiContentChars;

    @Param({"0", "1", "2", "3"})
    private int start;

    @Param({"4", "16", "32", "128"})
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
    public String stringFromChars() {
        return new String(asciiContentChars, start, end - start);
    }

    @Benchmark
    public String stringFromBytes() {
        return new String(asciiContent, 0, start, end - start);
    }

    @Benchmark
    public String stringFromBytesLessBoundCheck() {
        final byte[] asciiContent = this.asciiContent;
        if (start == 0 && end == asciiContent.length) {
            return new String(asciiContent, 0, 0, asciiContent.length);
        }
        return new String(asciiContent, 0, start, end - start);
    }


}
