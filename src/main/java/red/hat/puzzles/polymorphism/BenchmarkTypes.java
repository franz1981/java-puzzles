package red.hat.puzzles.polymorphism;

import java.util.function.ToIntFunction;

public class BenchmarkTypes {

    public static final ToIntFunction<Object> SWITCH_CLASS = SwitchClassUtil.createSwitchOf(
            LengthBytes0.class,
            LengthBytes1.class,
            LengthBytes2.class,
            LengthBytes3.class,
            LengthBytes4.class,
            LengthBytes5.class,
            LengthBytes6.class,
            LengthBytes7.class,
            LengthBytes8.class,
            LengthBytes9.class,
            LengthBytes10.class,
            LengthBytes11.class,
            LengthBytes12.class,
            LengthBytes13.class,
            LengthBytes14.class);

    public static final ClassValue<Integer> SWITCH_CLASS_VALUE = SwitchClassUtil.createClassValueSwitchOf(
            LengthBytes0.class,
            LengthBytes1.class,
            LengthBytes2.class,
            LengthBytes3.class,
            LengthBytes4.class,
            LengthBytes5.class,
            LengthBytes6.class,
            LengthBytes7.class,
            LengthBytes8.class,
            LengthBytes9.class,
            LengthBytes10.class,
            LengthBytes11.class,
            LengthBytes12.class,
            LengthBytes13.class,
            LengthBytes14.class);

    public static void main(String[] args) {
        ITableMegamorphicTests test = new ITableMegamorphicTests();
        test.types = 1;
        test.samples = 1000;
        test.setupSamples();
        test.do_TypeSwitch_Interface();
    }

    public interface LengthBytes {
        int lengthOf(byte[] b);
    }

    public static class LengthBytes0 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes1 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes2 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes3 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes4 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes5 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes6 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes7 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes8 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes9 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes10 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes11 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes12 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes13 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }

    public static class LengthBytes14 implements LengthBytes {
        @Override
        public int lengthOf(byte[] b) {
            return b.length;
        }
    }
}
