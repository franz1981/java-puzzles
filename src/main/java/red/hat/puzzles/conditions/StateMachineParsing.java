package red.hat.puzzles.conditions;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class StateMachineParsing {

    private interface ParameterConsumer {
        void accept(String s, int startName, int endName, int valueStart, int valueEnd);
    }

    @Param({"/updates?queries=5", "/updates?queries=5&foo=bar&cat=dog"})
    private String toParse;
    private ParameterConsumer consumer;

    private static void decodeParams(String uri, boolean hasPath, ParameterConsumer parameters, int paramsLimit, boolean semicolonIsNormalChar) {
        int pathEndIdx = hasPath ? findPathEndIndex(uri) : 0;
        decodeParams(uri, pathEndIdx, parameters, paramsLimit, semicolonIsNormalChar);
    }

    private static int findPathEndIndex(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }

    private static void decodeParams(String uri, int from, ParameterConsumer parameters, int paramsLimit, boolean semicolonIsNormalChar) {
        int len = uri.length();
        if (from >= len) {
            return;
        }
        if (uri.charAt(from) == '?') {
            from++;
        }
        decodeParams0(uri, from, parameters, paramsLimit, semicolonIsNormalChar);
    }

    private static void decodeParams0(String s, int from, ParameterConsumer parameters, int paramsLimit, boolean semicolonIsNormalChar) {
        int nameStart = from;
        int len = s.length();
        for (int p = 0; p < paramsLimit; p++) {
            int valueEndExclusive = -1;
            int indexOfEquals = -1;
            loop:
            for (int i = nameStart; i < len; i++) {
                // use a switch here, with less than MinJumpTableSize cases
                // the JIT will use a cascade of Ifs, but using
                switch (s.charAt(i)) {
                    // 0x3d or 61
                    case '=':
                        indexOfEquals = i;
                        break loop;
                    case ';':
                        // 0x3b or 59
                        if (semicolonIsNormalChar) {
                            continue;
                        }
                        // fall-through
                    case '&':
                        // 0x26 or 38
                        valueEndExclusive = i;
                        break loop;
                    case '#':
                        // 0x23 or 35
                        len = i;
                        break loop;
                }
            }
            int nextValueStart = -1;
            if (indexOfEquals != -1) {
                // we have found `=` first (which is quite common); we can drop on check
                nextValueStart = indexOfEquals + 1;
                for (int i = nextValueStart; i < len; i++) {
                    char ch = s.charAt(i);
                    if (ch == '&' || (!semicolonIsNormalChar && ch == ';')) {
                        valueEndExclusive = i;
                        break;
                    }
                    if (ch == '#') {
                        len = i;
                        break;
                    }
                }
            }
            if (valueEndExclusive == -1) {
                valueEndExclusive = len;
            }
            int valueStart;
            if (nextValueStart != -1) {
                valueStart = nextValueStart;
                if (valueStart == nameStart + 1) {
                    // uncommon slow path: it seems there is no name!
                    // search nameStart while skipping useless subsequent =, if any
                    nameStart = skipIf(s, valueStart, valueEndExclusive, '=');
                    valueStart = indexOf(s, nameStart + 1, valueEndExclusive, '=');
                }
            } else {
                valueStart = -1;
            }
            addParam(s, nameStart, valueStart, valueEndExclusive, parameters);
            if (valueEndExclusive == len) {
                break;
            }
            nameStart = valueEndExclusive + 1;
        }
    }

    private static int indexOf(String s, int from, int to, int ch) {
        for (int i = from; i < to; i++) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    private static int skipIf(String s, int from, int to, int ch) {
        for (int i = from; i < to; i++) {
            if (s.charAt(i) != ch) {
                return i;
            }
        }
        return to;
    }

    private static void addParam(String s, int nameStart, int valueStart, int valueEnd, ParameterConsumer parameters) {
        if (nameStart >= valueEnd) {
            return;
        }
        if (valueStart <= nameStart) {
            valueStart = valueEnd + 1;
        }
        if (findFirstEscaped(s, nameStart, valueStart - 1, false) != -1) {
            throw new IllegalStateException("ESCAPED NAME!");
        }
        if (findFirstEscaped(s, valueStart, valueEnd, false) != -1) {
            throw new IllegalStateException("ESCAPED VALUE!");
        }
        parameters.accept(s, nameStart, valueStart - 1, valueStart, valueEnd);
    }

    private static int findFirstEscaped(String s, int from, int toExcluded, boolean isPath) {
        for (int i = from; i < toExcluded; i++) {
            char c = s.charAt(i);
            if (c == '%' || c == '+' && !isPath) {
                return i;
            }
        }
        return -1;
    }

    @Setup
    public void init(Blackhole bh) {
        consumer = new ParameterConsumer() {
            @Override
            public void accept(String s, int startName, int endName, int valueStart, int valueEnd) {
                bh.consume(s);
                bh.consume(endName - startName);
                bh.consume(valueEnd - valueStart);
            }
        };
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void decodeParamaters() {
        decodeParams(toParse, true, consumer, 1024, false);
    }

}
