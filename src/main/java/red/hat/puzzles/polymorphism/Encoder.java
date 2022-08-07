package red.hat.puzzles.polymorphism;

import org.openjdk.jmh.annotations.CompilerControl;

public interface Encoder {

    void encode(Object o);

    interface HttpMessage {
    }

    interface HttpResponse extends HttpMessage {
    }

    interface HttpContent extends ReferenceCounted {
    }

    interface HttpLastContent extends HttpContent {
    }

    interface FullHttpMessage extends HttpMessage, HttpLastContent {

    }

    interface FullHttpResponse extends HttpResponse, FullHttpMessage {

    }
}
