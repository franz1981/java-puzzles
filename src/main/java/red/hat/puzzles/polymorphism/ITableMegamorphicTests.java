package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.*;

import static red.hat.puzzles.polymorphism.BenchmarkTypes.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class ITableMegamorphicTests {

    @Param("10000")
    int samples;

    @Param({"1", "2", "5", "9", "15"})
    int types;

    @Param({"false", "true"})
    boolean top;

    CallSample[] callSamples;

    @Setup
    public void setupSamples() {
        if (types > 15) {
            throw new IllegalArgumentException("cannot run tests with more then 15 types");
        }
        Random random = new Random(12345);
        List<CallSample> orderedSamples = new ArrayList<>(samples);
        // let's distribute samples, each one with equal probability
        byte[] data = new byte[0];
        for (int i = 0; i < samples; i++) {
            int type = random.nextInt(types);
            if (!top) {
                type = 14 - type;
            }
            orderedSamples.add(new CallSample(type, data));
        }
        Collections.shuffle(orderedSamples);
        callSamples = orderedSamples.toArray(new CallSample[0]);
    }

    @Benchmark
    public void do_Dynamic_Interface_Ref() {
        CallSample[] s = callSamples;
        int c = samples;
        for (int i = 0; i < c; i++) {
            s[i].do_Dynamic_Interface_Ref();
        }
    }

    @Benchmark
    public void do_Interface_ID_switch() {
        CallSample[] s = callSamples;
        int c = samples;
        for (int i = 0; i < c; i++) {
            s[i].do_Interface_ID_switch();
        }
    }

    @Benchmark
    public void do_Peel_Interface_Interface() {
        CallSample[] s = callSamples;
        int c = samples;
        for (int i = 0; i < c; i++) {
            s[i].do_Peel_Interface_Interface();
        }
    }

    @Benchmark
    public void do_TypeSwitch_Interface() {
        CallSample[] s = callSamples;
        int c = samples;
        for (int i = 0; i < c; i++) {
            s[i].do_TypeSwitch_Interface();
        }
    }

    @Benchmark
    public void do_TypeSwitch_ClassValue_Interface() {
        CallSample[] s = callSamples;
        int c = samples;
        for (int i = 0; i < c; i++) {
            s[i].do_TypeSwitch_ClassValue_Interface();
        }
    }


    public static class CallSample {
        private static final LengthBytes0 LengthBytes0 = new LengthBytes0();
        private static final LengthBytes1 LengthBytes1 = new LengthBytes1();
        private static final LengthBytes2 LengthBytes2 = new LengthBytes2();
        private static final LengthBytes3 LengthBytes3 = new LengthBytes3();
        private static final LengthBytes4 LengthBytes4 = new LengthBytes4();
        private static final LengthBytes5 LengthBytes5 = new LengthBytes5();
        private static final LengthBytes6 LengthBytes6 = new LengthBytes6();
        private static final LengthBytes7 LengthBytes7 = new LengthBytes7();
        private static final LengthBytes8 LengthBytes8 = new LengthBytes8();
        private static final LengthBytes9 LengthBytes9 = new LengthBytes9();
        private static final LengthBytes10 LengthBytes10 = new LengthBytes10();
        private static final LengthBytes11 LengthBytes11 = new LengthBytes11();
        private static final LengthBytes12 LengthBytes12 = new LengthBytes12();
        private static final LengthBytes13 LengthBytes13 = new LengthBytes13();
        private static final LengthBytes14 LengthBytes14 = new LengthBytes14();

        private final LengthBytes impl;
        private final int id;
        private final byte[] data;

        public CallSample(int id, byte[] data) {
            this.id = id;
            this.data = data;
            this.impl = interface_ID_Switch();
        }

        private static LengthBytes interface_ID_Switch(int id) {
            switch (id) {
                case 0:
                    return LengthBytes0;
                case 1:
                    return LengthBytes1;
                case 2:
                    return LengthBytes2;
                case 3:
                    return LengthBytes3;
                case 4:
                    return LengthBytes4;
                case 5:
                    return LengthBytes5;
                case 6:
                    return LengthBytes6;
                case 7:
                    return LengthBytes7;
                case 8:
                    return LengthBytes8;
                case 9:
                    return LengthBytes9;
                case 10:
                    return LengthBytes10;
                case 11:
                    return LengthBytes11;
                case 12:
                    return LengthBytes12;
                case 13:
                    return LengthBytes13;
                case 14:
                    return LengthBytes14;
                default:
                    throw new AssertionError();
            }
        }

        private LengthBytes interface_ID_Switch() {
            return interface_ID_Switch(id);
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int do_TypeSwitch_ClassValue_Interface() {
            final LengthBytes impl = this.impl;
            switch (SWITCH_CLASS_VALUE.get(impl.getClass())) {
                case 0:
                    return ((LengthBytes0) impl).lengthOf(data);
                case 1:
                    return ((LengthBytes1) impl).lengthOf(data);
                case 2:
                    return ((LengthBytes2) impl).lengthOf(data);
                case 3:
                    return ((LengthBytes3) impl).lengthOf(data);
                case 4:
                    return ((LengthBytes4) impl).lengthOf(data);
                case 5:
                    return ((LengthBytes5) impl).lengthOf(data);
                case 6:
                    return ((LengthBytes6) impl).lengthOf(data);
                case 7:
                    return ((LengthBytes7) impl).lengthOf(data);
                case 8:
                    return ((LengthBytes8) impl).lengthOf(data);
                case 9:
                    return ((LengthBytes9) impl).lengthOf(data);
                case 10:
                    return ((LengthBytes10) impl).lengthOf(data);
                case 11:
                    return ((LengthBytes11) impl).lengthOf(data);
                case 12:
                    return ((LengthBytes12) impl).lengthOf(data);
                case 13:
                    return ((LengthBytes13) impl).lengthOf(data);
                case 14:
                    return ((LengthBytes14) impl).lengthOf(data);
                default:
                    throw new AssertionError();

            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int do_TypeSwitch_Interface() {
            final LengthBytes impl = this.impl;
            switch (SWITCH_CLASS.applyAsInt(impl)) {
                case 0:
                    return ((LengthBytes0) impl).lengthOf(data);
                case 1:
                    return ((LengthBytes1) impl).lengthOf(data);
                case 2:
                    return ((LengthBytes2) impl).lengthOf(data);
                case 3:
                    return ((LengthBytes3) impl).lengthOf(data);
                case 4:
                    return ((LengthBytes4) impl).lengthOf(data);
                case 5:
                    return ((LengthBytes5) impl).lengthOf(data);
                case 6:
                    return ((LengthBytes6) impl).lengthOf(data);
                case 7:
                    return ((LengthBytes7) impl).lengthOf(data);
                case 8:
                    return ((LengthBytes8) impl).lengthOf(data);
                case 9:
                    return ((LengthBytes9) impl).lengthOf(data);
                case 10:
                    return ((LengthBytes10) impl).lengthOf(data);
                case 11:
                    return ((LengthBytes11) impl).lengthOf(data);
                case 12:
                    return ((LengthBytes12) impl).lengthOf(data);
                case 13:
                    return ((LengthBytes13) impl).lengthOf(data);
                case 14:
                    return ((LengthBytes14) impl).lengthOf(data);
                default:
                    throw new AssertionError();

            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int do_Interface_ID_switch() {
            final LengthBytes impl = this.impl;
            switch (id) {
                case 0:
                    return ((LengthBytes0) impl).lengthOf(data);
                case 1:
                    return ((LengthBytes1) impl).lengthOf(data);
                case 2:
                    return ((LengthBytes2) impl).lengthOf(data);
                case 3:
                    return ((LengthBytes3) impl).lengthOf(data);
                case 4:
                    return ((LengthBytes4) impl).lengthOf(data);
                case 5:
                    return ((LengthBytes5) impl).lengthOf(data);
                case 6:
                    return ((LengthBytes6) impl).lengthOf(data);
                case 7:
                    return ((LengthBytes7) impl).lengthOf(data);
                case 8:
                    return ((LengthBytes8) impl).lengthOf(data);
                case 9:
                    return ((LengthBytes9) impl).lengthOf(data);
                case 10:
                    return ((LengthBytes10) impl).lengthOf(data);
                case 11:
                    return ((LengthBytes11) impl).lengthOf(data);
                case 12:
                    return ((LengthBytes12) impl).lengthOf(data);
                case 13:
                    return ((LengthBytes13) impl).lengthOf(data);
                case 14:
                    return ((LengthBytes14) impl).lengthOf(data);
                default:
                    throw new AssertionError();

            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int do_Peel_Interface_Interface() {
            if (impl instanceof LengthBytes0) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes1) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes2) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes3) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes4) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes5) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes6) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes7) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes8) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes9) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes10) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes11) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes12) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes13) {
                return impl.lengthOf(data);
            }
            if (impl instanceof LengthBytes14) {
                return impl.lengthOf(data);
            }
            throw new IllegalStateException();
        }

        /**
         * This is required to ease ASM reading and save dead code elimination if the result won't be used
         */
        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int do_Dynamic_Interface_Ref() {
            return impl.lengthOf(data);
        }
    }

}
