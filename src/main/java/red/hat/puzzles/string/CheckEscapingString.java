package red.hat.puzzles.string;

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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(2)
public class CheckEscapingString {

    // TODO: optimize this for both archs
    private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private static final int ESCAPE_STANDARD = -1;
    /**
     * Lookup table used for determining which output characters in
     * 7-bit ASCII range need to be quoted.
     */
    protected final static int[] sOutputEscapes128;

    static {
        int[] table = new int[128];
        // Control chars need generic escape sequence
        for (int i = 0; i < 32; ++i) {
            table[i] = ESCAPE_STANDARD;
        }
        // Others (and some within that range too) have explicit shorter sequences
        table['"'] = '"';
        table['\\'] = '\\';
        // Escaping of slash is optional, so let's not add it: these are still within the previous [0-31] range!
        table[0x08] = 'b';
        table[0x09] = 't';
        table[0x0C] = 'f';
        table[0x0A] = 'n';
        table[0x0D] = 'r';
        sOutputEscapes128 = table;
    }

    String input;
    byte[] output;

    @Setup
    public void init() {
        input = " 09@P`p 09@P`p";
        output = new byte[input.length()];
        if (needEscapeBranchy(input, output)) {
            System.exit(1);
        }
    }

    @Benchmark
    public boolean needEscapeBranchy() {
        return needEscapeBranchy(input, output);
    }


    @Benchmark
    public boolean needEscapeBranchless() {
        return needEscapeBranchless(input, output);
    }

    private static boolean needEscapeBranchy(String input, byte[] output) {
        for (int i = 0; i < input.length(); i++) {
            int ch = input.charAt(i);
            if (ch > 0x7f || sOutputEscapes128[ch] != 0) {
                return true;
            }
            output[i] = (byte) ch;
        }
        return false;
    }

    private static boolean needEscapeBranchless(String input, byte[] output) {
        int batches = input.length() / 8;
        int off = 0;
        for (int i = 0; i < batches; i++) {
            final long batch1 = (((long) input.charAt(off)) << 48) |
                    (((long) input.charAt(off + 2)) << 32) |
                    input.charAt(off + 4) << 16 |
                    input.charAt(off + 6);
            final long batch2 = (((long) input.charAt(off + 1)) << 48) |
                    (((long) input.charAt(off + 3)) << 32) |
                    input.charAt(off + 5) << 16 |
                    input.charAt(off + 7);
            // pack the 8 bytes into a long, regardless
            long asciiValues = (batch1 << 8) | batch2;
            // from now one we reason like the bytes are all ascii
            // this is checking if we're withing the [0, 31] range: any negative value here have the 8th bit set
            long lessThan32 = asciiValues - 0x2020202020202020L;
            // the xor makes sure that we have 0 only for the bytes we search for: subtracting 1 will make the 8th bit set
            // table['"'] = '"'
            // NOTE on myself of the future: C2 can understand math and this trick to overflow isn't happening like this, on x86!
            long notQuote = (asciiValues ^ 0x2222222222222222L) - 0x0101010101010101L;
            // table['\\'] = '\\'
            long notBackslash = (asciiValues ^ 0x5C5C5C5C5C5C5C5CL) - 0x0101010101010101L;
            // filtering only the most significant bit of each byte
            // check if there's any non-ASCII char it will be in the high bytes
            long nonAscii = (batch1 | batch2) & 0xff80_ff80_ff80_ff80L;
            boolean toEscape = (nonAscii | ((lessThan32 | notQuote | notBackslash) & 0x8080808080808080L)) != 0;
            if (toEscape) {
                return true;
            }
            LONG.set(output, off, asciiValues);
            off += 8;
        }
        // use a loop for the rest
        for (int i = off; i < input.length(); i++) {
            int ch = input.charAt(i);
            if (ch > 0x7f || sOutputEscapes128[ch] != 0) {
                return true;
            }
            output[i] = (byte) ch;
        }
        return false;
    }
}
