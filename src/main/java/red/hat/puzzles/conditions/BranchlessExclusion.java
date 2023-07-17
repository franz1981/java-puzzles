package red.hat.puzzles.conditions;

import org.openjdk.jmh.annotations.*;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
public class BranchlessExclusion {
    private final static char[] RESERVED_CHARS = new char[]{'+', '#', '/', ';', '?', '&', ' ', '!', '=', '$', '|', '*', ':', '~', '-'};
    private static final int NOT_ASCII = 128;
    private static final boolean[] RESERVED = new boolean[256];

    static {
        for (int c : RESERVED_CHARS) {
            RESERVED[c] = true;
        }
        assert !RESERVED[NOT_ASCII];
    }

    private static int transformToASCIIOr128(char c) {
        int notAsciiMask = ((127 - c) >> 31);
        return (notAsciiMask & NOT_ASCII) | (~notAsciiMask & c);
    }

    public static boolean branchlessValidateLiteral(char c) {
        final int asciiOr128 = transformToASCIIOr128(c);
        // 0xFF is used to help the JIT to skip the bound checks on RESERVED
        // given that it can proce statically that any value produced will be < 256
        return !RESERVED[asciiOr128 & 0xFF];
    }

    public static boolean switchValidateLiteral(char c) {
        // ordered  , !, #, $, &, *, +, -, /, :, ;, =, ?, |, ~
        switch (c) {
            case ' ', '$', ':', '/', '-', '+', '*', '&', '#', '!', ';', '=', '?', '|', '~':
                return false;
            default:
                return true;
        }
    }

    public static boolean loopValidateLiteral(char c) {
        for (var reserved : RESERVED_CHARS) {
            if (c == reserved) {
                return false;
            }
        }
        return true;
    }

    @Param({"1", "1024"})
    public int inputs;

    @Param({"32"})
    public int length;

    private char[][] validChars;
    private int index;

    @Setup
    public void setup() {
        validChars = new char[inputs][];
        SplittableRandom rnd = new SplittableRandom(0);
        for (int i = 0; i < inputs; i++) {
            validChars[i] = new char[length];
            for (int j = 0; j < length; j++) {
                char c = (char) rnd.nextInt(Character.MIN_VALUE, ((int) Character.MAX_VALUE) + 1);
                while (!branchlessValidateLiteral(c)) {
                    c = (char) rnd.nextInt(Character.MIN_VALUE, ((int) Character.MAX_VALUE) + 1);
                }
                validChars[i][j] = c;
            }
        }
    }

    private char[] nextInput() {
        index++;
        if (index == inputs) {
            index = 0;
        }
        return validChars[index];
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean branchlessValidate() {
        var input = nextInput();
        for (var c : input) {
            if (!branchlessValidateLiteral(c)) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean switchValidate() {
        var input = nextInput();
        for (var c : input) {
            if (!switchValidateLiteral(c)) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public boolean loopValidate() {
        var input = nextInput();
        for (var c : input) {
            if (!loopValidateLiteral(c)) {
                return false;
            }
        }
        return true;
    }

}
