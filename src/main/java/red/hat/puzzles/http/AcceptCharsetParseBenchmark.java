package red.hat.puzzles.http;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

@State(Scope.Benchmark)
@Fork(2)
@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 10, time = 1)
public class AcceptCharsetParseBenchmark {
    private static final String[] ACCEPT_HEADERS = {
            "application/json",
            "application/json; charset=utf-8",
            "application/json, text/plain",
            "application/json, text/plain; q=0.8",
            "application/json;q=0.9, text/plain;q=0.5; charset=utf-8",
            "application/xml",
            "application/xml; charset=utf-8",
            "application/xml, text/plain",
            "application/xml, text/plain; q=0.8",
            "application/xml;q=0.9, text/plain;q=0.5; charset=utf-8",
            "application/octet-stream",
            "application/octet-stream; charset=utf-8",
            "application/octet-stream, text/plain",
            "application/octet-stream, text/plain; q=0.8",
            "application/octet-stream;q=0.9, text/plain;q=0.5; charset=utf-8",
            "text/html",
            "text/html; charset=utf-8",
            "text/plain",
            "text/plain; charset=utf-8",
            "text/csv",
            "text/csv; charset=utf-8",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/zip",
            "audio/mpeg",
            "audio/wav",
            "video/mp4",
            "video/quicktime",
            "application/octet-stream;q=0.5",
            "application/json;q=0.5",
            "text/plain;q=0.5",
            "application/xhtml+xml",
            "application/xhtml+xml; charset=utf-8",
            "text/xml",
            "text/xml; charset=utf-8",
            "text/css",
            "text/css; charset=utf-8",
            "application/javascript",
            "application/javascript; charset=utf-8",
            "application/x-www-form-urlencoded",
            "application/x-www-form-urlencoded; charset=utf-8",
            "multipart/form-data",
            "multipart/form-data; boundary=----WebKitFormBoundaryABC123",
            "application/graphql",
            "application/graphql; charset=utf-8",
            "application/vnd.api+json",
            "application/vnd.api+json; charset=utf-8",
            "application/ld+json",
            "application/ld+json; charset=utf-8",
            "application/rss+xml",
            "application/rss+xml; charset=utf-8",
            "application/atom+xml",
            "application/atom+xml; charset=utf-8",
            "application/geo+json",
            "application/geo+json; charset=utf-8",
            "application/vnd.geo+json",
            "application/vnd.geo+json; charset=utf-8",
            "application/rtf",
            "application/rtf; charset=utf-8",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.text; charset=utf-8",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.spreadsheet; charset=utf-8",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.presentation; charset=utf-8",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.oasis.opendocument.graphics; charset=utf-8",
            "application/x-latex",
            "application/x-latex; charset=utf-8",
            "application/x-dvi",
            "application/x-dvi; charset=utf-8"};

    private String[] mimeTypes;
    private String mimeTypeNoCharsetNoSemicolon;

    @Setup
    public void init() {
        mimeTypes = ACCEPT_HEADERS;
        mimeTypeNoCharsetNoSemicolon = "application/xml";
    }

    private static int indexOfSemicolonOrWhitespace(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == ';' || Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    private static String parseCharset(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        final int charsetIndex = mimeType.indexOf("charset=");
        if (charsetIndex == -1) {
            return null;
        }
        final int charsetValueStart = charsetIndex + 8;
        final int firstSemicolonWhitespace = indexOfSemicolonOrWhitespace(mimeType, charsetValueStart);
        final int charsetValueEnd = firstSemicolonWhitespace == -1 ? mimeType.length() : firstSemicolonWhitespace;
        final int charsetLen = charsetValueEnd - charsetValueStart;
        if (charsetLen == 0) {
            return null;
        }
        return charsetOf(mimeType, charsetValueStart, charsetValueEnd, charsetLen);
    }

    private static String charsetOf(String mimeType, int charsetValueStart, int charsetValueEnd, int charsetLen) {
        switch (charsetLen) {
            case 5:
                if (mimeType.regionMatches(charsetValueStart, "utf-8", 0, 5)) {
                    return "utf-8";
                }
                break;
            case 8:
                if (mimeType.regionMatches(charsetValueStart, "us-ascii", 0, 8)) {
                    return "us-ascii";
                }
                break;
        }
        return mimeType.substring(charsetValueStart, charsetValueEnd);
    }

    private static String getCharset(String mimeType) {
        final String parsedCharset = parseCharset(mimeType);
        if (parsedCharset != null) {
            return parsedCharset;
        }
        return StandardCharsets.UTF_8.name();
    }

    @Benchmark
    public void parseCharsetsNoSplit(Blackhole bh) {
        for (String mimeType : mimeTypes) {
            bh.consume(getCharset(mimeType));
        }
    }

    @Benchmark
    public void parseCharsetsSplit(Blackhole bh) {
        for (String mimeType : mimeTypes) {
            bh.consume(getCharsetSplit(mimeType));
        }
    }

    @Benchmark
    public void parseCharsetsTokenizer(Blackhole bh) {
        for (String mimeType : mimeTypes) {
            bh.consume(getCharsetTokenizer(mimeType));
        }
    }

    @Benchmark
    public String parseCharsetsNoSplitNoCharsetNoSemicolon() {
        return getCharset(mimeTypeNoCharsetNoSemicolon);
    }

    @Benchmark
    public String parseCharsetsSplitNoCharsetNoSemicolon() {
        return getCharsetSplit(mimeTypeNoCharsetNoSemicolon);
    }

    @Benchmark
    public String parseCharsetsTokenizerNoCharsetNoSemicolon() {
        return getCharsetTokenizer(mimeTypeNoCharsetNoSemicolon);
    }

    private static String getCharsetSplit(String mimeType) {
        if (mimeType != null && mimeType.contains(";")) {
            String[] parts = mimeType.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("charset")) {
                    return part.split("=")[1];
                }
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    private static String getCharsetTokenizer(String mimeType) {
        if (mimeType != null) {
            var charsets = new StringTokenizer(mimeType, ";", false);
            while (charsets.hasMoreTokens()) {
                final String charset = charsets.nextToken().trim();
                if (charset.startsWith("charset=")) {
                    return charset.substring(8);
                }
            }
        }
        return StandardCharsets.UTF_8.name();
    }

}