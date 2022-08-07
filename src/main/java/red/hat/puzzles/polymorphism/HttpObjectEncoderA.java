package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.CompilerControl;

import static red.hat.puzzles.polymorphism.ReferenceCounted.release;
import static red.hat.puzzles.polymorphism.Encoder.HttpMessage;

public class HttpObjectEncoderA<H extends HttpMessage> implements Encoder {
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Override
    public void encode(Object o) {
        try {
            if (o instanceof HttpMessage) {
                H msg = (H) o;
                encodeHttpMessage(msg);
            }
            if (o instanceof HttpContent) {
                if (o instanceof HttpLastContent) {
                    encodeLastHttpContent((HttpLastContent) o);
                } else {
                    encodeHttpNotLastContent((HttpContent) o);
                }
            }
        } finally {
            release(o);
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeHttpMessage(H msg) {
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeLastHttpContent(HttpLastContent content) {

    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeHttpNotLastContent(HttpContent content) {

    }
}
