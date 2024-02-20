package red.hat.puzzles.checks;

import java.nio.charset.StandardCharsets;

public final class AsciiString {
    private final byte[] ascii;
    private final int offset;
    private final int length;

    public AsciiString(String string) {
        this.ascii = string.getBytes(StandardCharsets.US_ASCII);
        this.offset = 0;
        this.length = ascii.length;
    }

    private static boolean equalsIgnoreCase(byte a, byte b) {
        return a == b || toLowerCase(a) == toLowerCase(b);
    }

    private static byte toLowerCase(byte b) {
        return isUpperCase(b) ? (byte) (b + 32) : b;
    }

    public static boolean isUpperCase(byte value) {
        return value >= 'A' && value <= 'Z';
    }

    public boolean unoptimizedContentEqualsIgnoreCase(AsciiString other) {
        if (this == other) {
            return true;
        }

        if (other == null || other.length() != length()) {
            return false;
        }

        for (int i = 0; i < length(); ++i) {
            if (!equalsIgnoreCase(ascii[i], other.ascii[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean optimizedContentEqualsIgnoreCase(AsciiString other) {
        if (this == other) {
            return true;
        }

        if (other == null || other.length() != length()) {
            return false;
        }

        byte[] ascii = this.ascii;
        byte[] otherAscii = other.ascii;
        if ((offset | other.offset) == 0 && length == ascii.length) {
            for (int i = 0; i < ascii.length; ++i) {
                if (!equalsIgnoreCase(ascii[i], otherAscii[i])) {
                    return false;
                }
            }
            return true;
        }
        return misalignedEqualsIgnoreCase(other, ascii, otherAscii);
    }

    private boolean misalignedEqualsIgnoreCase(AsciiString other, byte[] value, byte[] rhsValue) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int arrayOffset() {
        return offset;
    }

    public int length() {
        return length;
    }
}
