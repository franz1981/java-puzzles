package red.hat.puzzles.string;

public class StringUtilTable {

    public static final class ResizableByteArray {

        private byte[] array;

        public ResizableByteArray(int initialSize) {
            this.array = new byte[initialSize];
        }

        public void set(int index, byte value) {
            array[index] = value;
        }

        public void ensureCapacity(int capacity) {
            if (array.length < capacity) {
                byte[] newArray = new byte[capacity];
                System.arraycopy(array, 0, newArray, 0, array.length);
                array = newArray;
            }
        }

        public String toUsAsciiString(int length) {
            return new String(array, 0, 0, length);
        }
    }

    // this is accounting for Latin1 chars only
    private static final byte[] NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS = new byte[256];

    static {
        // replace every non alpha-numeric latin char by an underscore
        for (int c = 0; c < 256; c++) {
            if ('a' <= c && c <= 'z' ||
                    'A' <= c && c <= 'Z' ||
                    '0' <= c && c <= '9') {
                NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS[c] = (byte) c;
            } else {
                NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS[c] = '_';
            }
        }
    }

    public static boolean isAsciiLetterOrDigit(char c) {
        if (c > 255) {
            return false;
        }
        return NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS[c & 0xFF] != '_';
    }

    private static char replacementOf(char c) {
        if (c > 255) {
            return '_';
        }
        return (char) (((int) NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS[c & 0xFF]) & 0xFF);
    }

    private static byte rawReplacementOf(char c) {
        if (c > 255) {
            return '_';
        }
        return NON_ALPHANUMERIC_UNDERSCORE_REPLACEMENTS[c & 0xFF];
    }

    public static String replaceNonAlphanumericByUnderscores(final String name) {
        // accounts for the worst case scenario
        byte[] usAsciiResult = new byte[name.length() + 1];
        int length = name.length();
        // bogus value
        char c = 0;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);
            usAsciiResult[i] = rawReplacementOf(c);
        }
        if (c == '"') {
            usAsciiResult[length] = '_';
            return new String(usAsciiResult, 0, 0, usAsciiResult.length);
        } else {
            return new String(usAsciiResult, 0, 0, usAsciiResult.length - 1);
        }
    }

    public static String replaceNonAlphanumericByUnderscores(final String name, final StringBuilder sb) {
        int length = name.length();
        // bogus value
        char c = 0;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);
            sb.append(replacementOf(c));
        }
        if (c == '"') {
            sb.append('_');
        }
        return sb.toString();
    }

    public static String replaceNonAlphanumericByUnderscores(final String name, final ResizableByteArray sb) {
        // size it accounting for worst case scenario
        int length = name.length();
        sb.ensureCapacity(length + 1);
        // bogus value
        char c = 0;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);
            sb.set(i, rawReplacementOf(c));
        }
        if (c == '"') {
            sb.set(length, (byte) '_');
            return sb.toUsAsciiString(length + 1);
        } else {
            return sb.toUsAsciiString(length);
        }
    }

}
