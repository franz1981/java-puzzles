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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LowerCaseComparison {

    /**
     * Run me with: -psame=true --jvmArgs="-XX:LoopMaxUnroll=0" -prof perfnorm
     *
     * The ASM produced for the 2 loops is
     *
     *           ↗│  0x00007f502121ab00:   movslq %ebp,%rdx
     *   3.23%   ││  0x00007f502121ab03:   movsbl 0x10(%r11,%rdx,1),%r8d       ;*baload {reexecute=0 rethrow=0 return_oop=0}
     *           ││                                                            ; - red.hat.puzzles.checks.AsciiString::optimizedContentEqualsIgnoreCase@68 (line 58)
     *           ││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::optimizedContentEqualsIgnoreCase@8 (line 47)
     *  38.40%   ││  0x00007f502121ab09:   movsbl 0x10(%r10,%rdx,1),%edx       ;*baload {reexecute=0 rethrow=0 return_oop=0}
     *           ││                                                            ; - red.hat.puzzles.checks.AsciiString::optimizedContentEqualsIgnoreCase@72 (line 58)
     *           ││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::optimizedContentEqualsIgnoreCase@8 (line 47)
     *  13.70%   ││  0x00007f502121ab0f:   cmp    %edx,%r8d
     *  38.86%  ╭││  0x00007f502121ab12:   jne    0x00007f502121ab41           ;*if_icmpeq {reexecute=0 rethrow=0 return_oop=0}
     *          │││                                                            ; - red.hat.puzzles.checks.AsciiString::equalsIgnoreCase@2 (line 17)
     *          │││                                                            ; - red.hat.puzzles.checks.AsciiString::optimizedContentEqualsIgnoreCase@73 (line 58)
     *          │││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::optimizedContentEqualsIgnoreCase@8 (line 47)
     *          │││  0x00007f502121ab14:   inc    %ebp                         ;*iinc {reexecute=0 rethrow=0 return_oop=0}
     *          │││                                                            ; - red.hat.puzzles.checks.AsciiString::optimizedContentEqualsIgnoreCase@81 (line 57)
     *          │││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::optimizedContentEqualsIgnoreCase@8 (line 47)
     *   0.05%  │││  0x00007f502121ab16:   cmp    %r13d,%ebp
     *          │╰│  0x00007f502121ab19:   jl     0x00007f502121ab00           ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
     *
     *      VS
     *
     *            ↗│  0x00007fc98921c120:   movsbl 0x10(%r8,%rax,1),%r10d       ;*baload {reexecute=0 rethrow=0 return_oop=0}
     *            ││                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@53 (line 38)
     *            ││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *   6.44%    ││  0x00007fc98921c126:   mov    %eax,%r11d
     *   0.93%    ││  0x00007fc98921c129:   add    %edx,%r11d                   ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
     *            ││                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@48 (line 38)
     *            ││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *   0.05%    ││  0x00007fc98921c12c:   movsbl 0x10(%rsi,%r11,1),%r9d       ;*baload {reexecute=0 rethrow=0 return_oop=0}
     *            ││                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@59 (line 38)
     *            ││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *  39.53%    ││  0x00007fc98921c132:   cmp    %r9d,%r10d
     *  47.87%   ╭││  0x00007fc98921c135:   jne    0x00007fc98921c166           ;*if_icmpeq {reexecute=0 rethrow=0 return_oop=0}
     *           │││                                                            ; - red.hat.puzzles.checks.AsciiString::equalsIgnoreCase@2 (line 17)
     *           │││                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@60 (line 38)
     *           │││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *           │││  0x00007fc98921c137:   inc    %eax                         ;*iinc {reexecute=0 rethrow=0 return_oop=0}
     *           │││                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@68 (line 37)
     *           │││                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *   0.08%   │││  0x00007fc98921c139:   cmp    %ebp,%eax
     *   0.18%   │╰│  0x00007fc98921c13b:   jl     0x00007fc98921c120           ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
     *           │ │                                                            ; - red.hat.puzzles.checks.AsciiString::unoptimizedContentEqualsIgnoreCase@45 (line 37)
     *           │ │                                                            ; - red.hat.puzzles.checks.LowerCaseComparison::unoptimizedContentEqualsIgnoreCase@8 (line 41)
     *
     * Which shows that
     * - the optimized version has a data dependency on the %rdx register, which is used as index by both array's load, but as destination for the second one,
     * preventing both loads to be executed in parallel
     * - the unpotimized version doesn't have such data dependency and the cycles counters indeed is accumulated after both loads,
     * sign of more skidding and better pipeline usage
     *
     * Running perfnorm with 4096 values over both indeed shows these interesting differences:
     *
     * LowerCaseComparison.optimizedContentEqualsIgnoreCase                              true    4096  avgt   10   1961.759 ±  5.141      ns/op
     * LowerCaseComparison.optimizedContentEqualsIgnoreCase:instructions                 true    4096  avgt       31568.731                #/op
     * LowerCaseComparison.optimizedContentEqualsIgnoreCase:stalled-cycles-backend       true    4096  avgt           8.414                #/op
     * LowerCaseComparison.optimizedContentEqualsIgnoreCase:stalled-cycles-frontend      true    4096  avgt           3.048                #/op
     *
     * LowerCaseComparison.unoptimizedContentEqualsIgnoreCase                            true    4096  avgt   10   1765.949 ± 14.117      ns/op
     * LowerCaseComparison.unoptimizedContentEqualsIgnoreCase:instructions               true    4096  avgt       35540.522                #/op
     * LowerCaseComparison.unoptimizedContentEqualsIgnoreCase:stalled-cycles-backend     true    4096  avgt           7.364                #/op
     * LowerCaseComparison.unoptimizedContentEqualsIgnoreCase:stalled-cycles-frontend    true    4096  avgt           1.128                #/op
     *
     * Which indeed point to a slightly more stalled cycles on backend for the optimized version, but nearly 3 times more on frontend, likely due to the
     * data dependency.
     *
     * A proper top-down analysis should be performed to pin-point exactly what's going on.
     *
     * Sadly on AMD it requires using a slightly different naming as
     *
     * $ perf stat -M PipelineL1
     *
     * See https://perf.wiki.kernel.org/index.php/Top-Down_Analysis for more details on the Intel counter-part.
     *
     *
     *
     *
     *
     */

    @Param({"false", "true"})
    public boolean same;

    @Param({"8", "4096"})
    public int size;
    private AsciiString asciiStrings;
    private AsciiString otherAscii;

    @Setup
    public void init() {
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 'c');
        String string = new String(bytes);
        asciiStrings = new AsciiString(string);
        if (same) {
            otherAscii = new AsciiString(string);
        } else {
            bytes[0] = (byte) 'C';
            otherAscii = new AsciiString(new String(bytes));
        }
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
