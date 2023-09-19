package red.hat.puzzles.string;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(2)
public class ToLowerCaseAndDotted {

    public String name;
    public StringBuilder sb;

    @Setup
    public void setUp() {
        name = "MY_ENV_VARIABLE_FOO";
        sb = new StringBuilder(name.length());
    }

    @Benchmark
    public Object toLowerCaseAndDottedBuilderReuse() {
        var sb = this.sb;
        sb.setLength(0);
        return toLowerCaseAndDotted(name, sb);
    }

    @Benchmark
    public Object toLowerCaseAndDottedBuilderAsBytesReuse() {
        var sb = this.sb;
        sb.setLength(0);
        return toLowerCaseAndDottedBuilderAsBytes(name, sb);
    }

    @Benchmark
    public Object toLowerCaseAndDottedBytesNoReuse() {
        return toLowerCaseAndDottedBytes(name);
    }


    public static String toLowerCaseAndDotted(final String name) {
        return toLowerCaseAndDotted(name, new StringBuilder(name.length()));
    }

    public static String toLowerCaseAndDotted(final String name, final StringBuilder sb) {
        int length = name.length();
        int beginSegment = 0;
        boolean quotesOpen = false;
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                if (i == 0) {
                    // leading _ can only mean a profile
                    sb.append('%');
                    continue;
                }

                // Do not convert to index if the first segment is a number
                if (beginSegment > 0) {
                    if (isNumeric(sb, beginSegment, i)) {
                        sb.setCharAt(beginSegment - 1, '[');
                        sb.append(']');

                        int j = i + 1;
                        if (j < length) {
                            if ('_' == name.charAt(j)) {
                                sb.append('.');
                                i = j;
                            }
                        }

                        continue;
                    }
                }

                int j = i + 1;
                if (j < length) {
                    if ('_' == name.charAt(j) && !quotesOpen) {
                        sb.append('.');
                        sb.append('\"');
                        i = j;
                        quotesOpen = true;
                    } else if ('_' == name.charAt(j) && quotesOpen) {
                        sb.append('\"');
                        // Ending
                        if (j + 1 < length) {
                            sb.append('.');
                        }
                        i = j;
                        quotesOpen = false;
                    } else {
                        sb.append('.');
                    }
                } else {
                    sb.append('.');
                }
                beginSegment = j;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static boolean isNumeric(CharSequence digits) {
        return isNumeric(digits, 0, digits.length());
    }

    public static boolean isNumeric(CharSequence digits, int start, int end) {
        if (digits.length() == 0) {
            return false;
        }

        for (int i = start; i < end; i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String toLowerCaseAndDottedBuilderAsBytes(final String name, StringBuilder result) {
        // same as toLowerCaseAndDottedBytes but with a StringBuilder instead of a byte[] and setCharAt instead of appending
        int length = name.length();

        if (length == 0) {
            return name;
        }

        if (length > 1 && name.charAt(length - 1) == '_' && name.charAt(length - 2) == '_') { // last quoted segment
            length--;
        }
        result.setLength(length);

        int i = 0;
        if (name.charAt(0) == '_') { // starting _ is a profile
            result.setCharAt(0, '%');
            i++;
        }

        boolean quotesOpen = false;
        for (; i < length; i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                int next = i + 1;
                if (quotesOpen) {
                    if (next == length) {
                        result.setCharAt(i, '"'); // ending quotes
                    } else if (name.charAt(next) == '_') { // double _ end quote
                        result.setCharAt(i, '"');
                        result.setCharAt(next, '.');
                        i++;
                        quotesOpen = false;
                    } else {
                        result.setCharAt(i, '.');
                    }
                } else if (next < length) {
                    char d = name.charAt(next);
                    if (Character.isDigit(d)) { // maybe index
                        result.setCharAt(next, d);
                        int j = next + 1;
                        for (; j < length; j++) {
                            d = name.charAt(j);
                            if (Character.isDigit(d)) { // index
                                result.setCharAt(j, d);
                            } else if ('_' == d) { // ending index
                                result.setCharAt(i, '[');
                                result.setCharAt(j, ']');
                                i = j;
                                break;
                            } else { // not an index
                                result.setCharAt(i, '.');
                                i = j;
                                break;
                            }
                        }

                    } else if (name.charAt(next) == '_') { // double _ start quote
                        result.setCharAt(i, '.');
                        result.setCharAt(next, '"');
                        i++;
                        quotesOpen = true;
                    } else {
                        result.setCharAt(i, '.');
                    }
                } else {
                    result.setCharAt(i, '.');
                }
            } else {
                result.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    public static String toLowerCaseAndDottedBytes(final String name) {
        int length = name.length();

        if (length == 0) {
            return name;
        }

        byte[] result;
        if (length > 1 && name.charAt(length - 1) == '_' && name.charAt(length - 2) == '_') { // last quoted segment
            length--;
        }
        result = new byte[length];

        int i = 0;
        if (name.charAt(0) == '_') { // starting _ is a profile
            result[0] = '%';
            i++;
        }

        boolean quotesOpen = false;
        for (; i < length; i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                int next = i + 1;
                if (quotesOpen) {
                    if (next == length) {
                        result[i] = '"'; // ending quotes
                    } else if (name.charAt(next) == '_') { // double _ end quote
                        result[i] = '"';
                        result[next] = '.';
                        i++;
                        quotesOpen = false;
                    } else {
                        result[i] = '.';
                    }
                } else if (next < length) {
                    char d = name.charAt(next);
                    if (Character.isDigit(d)) { // maybe index
                        result[next] = (byte) d;
                        int j = next + 1;
                        for (; j < length; j++) {
                            d = name.charAt(j);
                            if (Character.isDigit(d)) { // index
                                result[j] = (byte) d;
                            } else if ('_' == d) { // ending index
                                result[i] = '[';
                                result[j] = ']';
                                i = j;
                                break;
                            } else { // not an index
                                result[i] = '.';
                                i = j;
                                break;
                            }
                        }

                    } else if (name.charAt(next) == '_') { // double _ start quote
                        result[i] = '.';
                        result[next] = '"';
                        i++;
                        quotesOpen = true;
                    } else {
                        result[i] = '.';
                    }
                } else {
                    result[i] = '.';
                }
            } else {
                result[i] = (byte) Character.toLowerCase(c);
            }
        }

        return new String(result);
    }
}
