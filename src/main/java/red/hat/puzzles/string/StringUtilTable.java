package red.hat.puzzles.string;

public class StringUtilTable {

    public static final class ResizableByteArray {

        private byte[] array;

        public ResizableByteArray(int initialSize) {
            this.array = new byte[initialSize];
        }

        private byte[] ensureCapacity(int capacity) {
            byte[] array = this.array;
            if (array.length < capacity) {
                byte[] newArray = new byte[capacity];
                System.arraycopy(array, 0, newArray, 0, array.length);
                this.array = newArray;
                return newArray;
            }
            return array;
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

    public static String replaceNonAlphanumericByUnderscores(final String name) {
        final int length = name.length();
        if (length == 0) {
            return name;
        }
        // size it accounting for worst case scenario
        final byte[] result = new byte[length + 1];
        return replaceNonAlphanumericByteUnderscoresWithByteArray(name, length, result);
    }

    public static String replaceNonAlphanumericByUnderscores(final String name, final ResizableByteArray sb) {
        int length = name.length();
        if (length == 0) {
            return name;
        }
        // size it accounting for worst case scenario
        return replaceNonAlphanumericByteUnderscoresWithByteArray(name, length, sb.ensureCapacity(length + 1));
    }

    private static String replaceNonAlphanumericByteUnderscoresWithByteArray(String name, int length, byte[] ascii) {
        char c = 0;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);
            ascii[i] = rawReplacementOf(c);
        }
        if (c == '"') {
            ascii[length] = '_';
            return new String(ascii, 0, 0, length + 1);
        } else {
            return new String(ascii, 0, 0, length);
        }
    }

}
