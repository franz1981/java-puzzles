package red.hat.puzzles.string;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(4)
public class StringBuilderVsFormat {

    private static final String EXTERNALIZED_PREFIX = "ExternalizedType";

    // Simulated fixed values
    private int count;
    private String typeClassName;
    private int defaultSqlTypeCode;

    @Setup(Level.Trial)
    public void setup() {
        count = 12345;
        typeClassName = "java.lang.String";
        defaultSqlTypeCode = 4;
    }

    @Benchmark
    public String stringBuilderWithPrecomputedSize() {
        int chars = EXTERNALIZED_PREFIX.length() + 1 // '@'
                    + stringSize(count)
                    + 2 // '(' and ','
                    + typeClassName.length()
                    + stringSize(defaultSqlTypeCode)
                    + 1; // ')'
        StringBuilder nameBuilder = new StringBuilder(chars);
        nameBuilder.append(EXTERNALIZED_PREFIX)
                .append('@')
                .append(count)
                .append('(')
                .append(typeClassName)
                .append(',')
                .append(defaultSqlTypeCode)
                .append(')');
        return nameBuilder.toString();
    }

    @Benchmark
    public String stringFormat() {
        return String.format(
                Locale.ROOT,
                "%s@%s(%s,%s)",
                EXTERNALIZED_PREFIX,
                count,
                typeClassName,
                defaultSqlTypeCode
        );
    }

    @Benchmark
    public String stringBuilderDefault() {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(EXTERNALIZED_PREFIX)
                .append('@')
                .append(count)
                .append('(')
                .append(typeClassName)
                .append(',')
                .append(defaultSqlTypeCode)
                .append(')');
        return nameBuilder.toString();
    }

    @Benchmark
    public String stringConcat() {
        return EXTERNALIZED_PREFIX + '@' + count + '(' + typeClassName + ',' + defaultSqlTypeCode + ')';
    }

    public static int stringSize(int x) {
        int sign = 1;
        if (x >= 0) {
            sign = 0;
            x = -x;
        }
        if (x >= -10) {
            return sign + 1;
        }
        if (x >= -100) {
            return sign + 2;
        }
        if (x >= -1000) {
            return sign + 3;
        }
        if (x >= -10000) {
            return sign + 4;
        }
        if (x >= -100000) {
            return sign + 5;
        }
        if (x >= -1000000) {
            return sign + 6;
        }
        if (x >= -10000000) {
            return sign + 7;
        }
        if (x >= -100000000) {
            return sign + 8;
        }
        if (x >= -1000000000) {
            return sign + 9;
        }
        return sign + 10;
    }
}

