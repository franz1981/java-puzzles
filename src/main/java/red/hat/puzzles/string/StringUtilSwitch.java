package red.hat.puzzles.string;

public class StringUtilSwitch {

    public static boolean isAsciiLetterOrDigit(char c) {
        return 'a' <= c && c <= 'z' ||
                'A' <= c && c <= 'Z' ||
                '0' <= c && c <= '9';
    }

    public static String replaceNonAlphanumericByUnderscores(final String name) {
        return replaceNonAlphanumericByUnderscores(name, new StringBuilder(name.length()));
    }

    public static String replaceNonAlphanumericByUnderscores(final String name, final StringBuilder sb) {
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (isAsciiLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append('_');
                if (c == '"' && i + 1 == length) {
                    sb.append('_');
                }
            }
        }
        return sb.toString();
    }
}
