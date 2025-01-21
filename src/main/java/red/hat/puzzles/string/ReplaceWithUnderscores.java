package red.hat.puzzles.string;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Mode.AverageTime;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(AverageTime)
public class ReplaceWithUnderscores {

    private static final int SEED = 42;

    private static byte[] alphanumericChars() {
        byte[] chars = new byte[62];
        int index = 0;
        for (int c = 'a'; c <= 'z'; c++) {
            chars[index++] = (byte) c;
        }
        for (int c = 'A'; c <= 'Z'; c++) {
            chars[index++] = (byte) c;
        }
        for (int c = '0'; c <= '9'; c++) {
            chars[index++] = (byte) c;
        }
        return chars;
    }

    private static byte[] nonAlphanumericChars(byte[] alphaNumericChars) {
        BitSet nonAlphanum = new BitSet(256);
        // set the alpha numeric chars
        for (byte c : alphaNumericChars) {
            nonAlphanum.set(Byte.toUnsignedInt(c));
        }
        // flip the bits
        nonAlphanum.flip(0, 256);
        // create the byte array
        byte[] nonAlphanumericChars = new byte[nonAlphanum.cardinality()];
        for (int i = nonAlphanum.nextSetBit(0), j = 0; i >= 0; i = nonAlphanum.nextSetBit(i + 1), j++) {
            nonAlphanumericChars[j] = (byte) i;
        }
        return nonAlphanumericChars;
    }

    public static String[] createDataSet(int count, int size, int nonAlphanumericProbability, int finalDoubleQuoteProbability) {
        String[] dataSet = new String[count];
        Random rnd = new Random(SEED);
        byte[] alphaNumericChars = alphanumericChars();
        byte[] nonAlphanumericChars = nonAlphanumericChars(alphaNumericChars);
        // account for the worst case scenario
        byte[] reusableBytes = new byte[size + 1];
        for (int i = 0; i < count; i++) {
            dataSet[i] = generateString(rnd, size, nonAlphanumericProbability, finalDoubleQuoteProbability, reusableBytes, alphaNumericChars, nonAlphanumericChars);
        }
        return dataSet;
    }

    // generate the string based on the probabilities configured
    private static String generateString(Random rnd, int size, int nonAlphanumericProbability, int finalDoubleQuoteProbability, byte[] reusableBytes, byte[] alphaNumericChars, byte[] nonAlphanumericChars) {
        for (int i = 0; i < size; i++) {
            // let's check first if we should pick any special char
            int pickSpecialChar = rnd.nextInt(100);
            if (pickSpecialChar < nonAlphanumericProbability) {
                // it is a non-alphanumeric char
                if (i + 1 == size) {
                    if (rnd.nextInt(100) < finalDoubleQuoteProbability) {
                        // last char and we need to add a double quote
                        reusableBytes[i] = '"';
                    } else {
                        // i know is not great, but at least would avoid using " if it shouldn't
                        reusableBytes[i] = 0;
                    }
                } else {
                    // pick random non-alphanumeric char
                    byte ch = nonAlphanumericChars[rnd.nextInt(nonAlphanumericChars.length)];
                    reusableBytes[i] = ch;
                }
            } else {
                // pick random non-special char
                byte ch = alphaNumericChars[rnd.nextInt(alphaNumericChars.length)];
                reusableBytes[i] = ch;
            }
        }
        return new String(reusableBytes, 0, size);
    }

    @Param({"10000"})
    private int samples;

    @Param({"33"})
    private int size;

    @Param({"10"})
    private int nonAlphanumericProbability;

    @Param({"10"})
    private int finalDoubleQuoteProbability;

    private String[] dataSet;
    private int nextInput;
    private StringBuilder builder;
    private StringUtilTable.ResizableByteArray asciiByteArray;

    @Setup
    public void init() {
        dataSet = createDataSet(samples, size, nonAlphanumericProbability, finalDoubleQuoteProbability);
        nextInput = 0;
        builder = new StringBuilder(size + 1);
        asciiByteArray = new StringUtilTable.ResizableByteArray(size + 1);
    }

    private String nextInput() {
        String[] inputs = dataSet;
        int index = nextInput;
        index++;
        if (index >= inputs.length) {
            index = 0;
        }
        nextInput = index;
        return inputs[index];
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    public String replaceWithUnderscoresSwitch() {
        String value = nextInput();
        var builder = this.builder;
        builder.setLength(0);
        return StringUtilSwitch.replaceNonAlphanumericByUnderscores(value, builder);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    public String replaceWithUnderscoresTableSb() {
        String value = nextInput();
        var builder = this.builder;
        builder.setLength(0);
        return StringUtilTable.replaceNonAlphanumericByUnderscores(value, builder);
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Benchmark
    public String replaceWithUnderscoresTableArray() {
        String value = nextInput();
        var builder = this.asciiByteArray;
        return StringUtilTable.replaceNonAlphanumericByUnderscores(value, builder);
    }
}
