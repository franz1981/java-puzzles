package red.hat.puzzles.profilers;

import java.io.IOException;

public class FlamesByExample {

    private static volatile long consumedCPU;
    private static final long WORKS = 10;

    public static void main(String[] args) throws IOException {
        /**
         * Run me with:
         * -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+PreserveFramePointer
         *
         * perf-map-agent instructions:
         * <p>
         * -XX:+PreserveFramePointer -XX:+PrintGCApplicationStoppedTime -XX:+PrintSafepointStatistics -XX:-UseBiasedLocking -XX:-UseCounterDecay -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
         * <p>
         * $ jps
         * Take the process pid and use it with:
         * <p>
         * $ PERF_COLLAPSE_OPTS="--kernel --tid" PERF_RECORD_FREQ=99 PERF_RECORD_SECONDS=10 PERF_MAP_OPTIONS=unfoldall ~/perf-map-agent/bin/perf-java-flames <pid>
         */
        //warmup it until C2 compiled
        for (int i = 0; i < 100_000; i++) {
            a();
        }
        Thread profiledRunner = new Thread(() -> {
            final Thread currentThread = Thread.currentThread();
            while (!currentThread.isInterrupted()) {
                a();
            }
        });
        profiledRunner.setDaemon(true);
        profiledRunner.start();
        try {
            System.out.println("press one key to stop it...");
            System.in.read();
        } finally {
            profiledRunner.interrupt();
        }   
    }

    private static void a() {
        b();
        h();
    }

    private static void b() {
        c();
    }

    private static void c() {
        d();
    }

    private static void d() {
        e();
        f();
        //manually inlined version of BlackHole::consumeCPU
        long t = consumedCPU;
        for (long i = WORKS; i > 0L; --i) {
            t += t * 25214903917L + 11L + i & 281474976710655L;
        }
        if (t == 42L) {
            consumedCPU += t;
        }
    }

    private static void e() {
        //manually inlined version of BlackHole::consumeCPU
        long t = consumedCPU;
        for (long i = WORKS; i > 0L; --i) {
            t += t * 25214903917L + 11L + i & 281474976710655L;
        }
        if (t == 42L) {
            consumedCPU += t;
        }
    }

    private static void f() {
        g();
    }

    private static void g() {
        //manually inlined version of 2*BlackHole::consumeCPU
        long t = consumedCPU;
        long works = WORKS * 2;
        for (long i = works; i > 0L; --i) {
            t += t * 25214903917L + 11L + i & 281474976710655L;
        }
        if (t == 42L) {
            consumedCPU += t;
        }
    }

    private static void h() {
        i();
    }

    private static void i() {
        //manually inlined version of BlackHole::consumeCPU
        long t = consumedCPU;
        for (long i = WORKS; i > 0L; --i) {
            t += t * 25214903917L + 11L + i & 281474976710655L;
        }
        if (t == 42L) {
            consumedCPU += t;
        }
    }
}
