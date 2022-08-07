package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.CompilerControl;

import static red.hat.puzzles.polymorphism.Encoder.HttpMessage;


public class HttpObjectEncoderB<H extends HttpMessage> implements Encoder {
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Override
    public void encode(Object o) {
        if (o instanceof FullHttpMessage) {
            encodeFullHttpMessage(o);
            return;
        }
        if (o instanceof HttpMessage) {
            H msg = (H) o;
            if (msg instanceof HttpLastContent) {
                encodeHttpMessageLastContent(msg);
            } else if (msg instanceof HttpContent) {
                encodeHttpMessageNotLastContent(msg);
            } else {
                encodeJustHttpMessage(msg);
            }
        } else {
            if (o instanceof HttpLastContent) {
                encodeJustHttpLastContent(o);
            } else if (o instanceof HttpContent) {
                encodeJustHttpNotLastContent(o);
            }
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeHttpMessage(H msg) {
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeContent(HttpContent httpContent) {

    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private void encodeLastContent(HttpLastContent httpContent) {

    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeJustHttpNotLastContent(Object o) {
        H msg = (H) o;
        encodeHttpMessage(msg);
        HttpContent content = (HttpContent) o;
        try {
            encodeContent(content);
        } finally {
            content.release();
        }
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeJustHttpLastContent(Object o) {
        H msg = (H) o;
        encodeHttpMessage(msg);
        HttpLastContent content = (HttpLastContent) o;
        try {
            encodeLastContent(content);
        } finally {
            content.release();
        }
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeJustHttpMessage(H msg) {
        encodeHttpMessage(msg);
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeHttpMessageNotLastContent(H res) {
        encodeHttpMessage(res);
        HttpContent content = (HttpContent) res;
        try {
            encodeContent(content);
        } finally {
            content.release();
        }
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeHttpMessageLastContent(H res) {
        encodeHttpMessage(res);
        HttpLastContent content = (HttpLastContent) res;
        try {
            encodeLastContent(content);
        } finally {
            content.release();
        }
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    private void encodeFullHttpMessage(Object o) {
        FullHttpMessage msg = (FullHttpMessage) o;
        try {
            final H m = (H) o;
            encodeHttpMessage(m);
            encodeLastContent(msg);
        } finally {
            msg.release();
        }
    }

}
