package red.hat.puzzles.conditions;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class EnumSwitch {
    public enum Level {
        DISABLED,
        SIMPLE,
        ADVANCED,
        PARANOID
    }

    public static Level UNSTABLE_LEVEL = Level.valueOf(System.getProperty("level", Level.DISABLED.name()));
    public static final Level STABLE_LEVEL = Level.valueOf(System.getProperty("level", Level.DISABLED.name()));

    private ByteBuffer buffer;

    @Setup
    public void init() {
        buffer = ByteBuffer.allocate(0);
    }

    @Benchmark
    public ByteBuffer decorateUnstable() {
        return decorateUnstableLevel(buffer);
    }

    @Benchmark
    public ByteBuffer decorateStable() {
        return decorateStableLevel(buffer);
    }

    @Benchmark
    public ByteBuffer decorateUnstableFastPath() {
        return decorateUnstableFastDisabled(buffer);
    }

    @Benchmark
    public ByteBuffer decorateStableFastPath() {
        return decorateStableFastDisabled(buffer);
    }

    public static ByteBuffer decorateUnstableFastDisabled(ByteBuffer buf) {
        final Level level = UNSTABLE_LEVEL;
        if (level == Level.DISABLED) {
            return buf;
        }
        return decorateLevel(level, buf);
    }

    public static ByteBuffer decorateStableFastDisabled(ByteBuffer buf) {
        final Level level = STABLE_LEVEL;
        if (level == Level.DISABLED) {
            return buf;
        }
        return decorateLevel(level, buf);
    }

    private static ByteBuffer decorateLevel(Level level, ByteBuffer buf) {
        Object track;
        switch (level) {
            case SIMPLE:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            case ADVANCED:
            case PARANOID:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            default:
                break;
        }
        return buf;
    }

    public static ByteBuffer decorateStableLevel(ByteBuffer buf) {
        Object track;
        switch (STABLE_LEVEL) {
            case SIMPLE:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            case ADVANCED:
            case PARANOID:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            default:
                break;
        }
        return buf;
    }

    public static ByteBuffer decorateUnstableLevel(ByteBuffer buf) {
        Object track;
        switch (UNSTABLE_LEVEL) {
            case SIMPLE:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            case ADVANCED:
            case PARANOID:
                track = track(buf);
                if (track != null) {
                    buf = decorate(buf, track);
                }
                break;
            default:
                break;
        }
        return buf;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static Object track(ByteBuffer buffer) {
        // nop, we just don't care
        return buffer;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static ByteBuffer decorate(ByteBuffer buffer, Object tracking) {
        // nop, we just don't care
        return buffer;
    }

}
