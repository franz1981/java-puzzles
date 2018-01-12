/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.hat.puzzles.profilers;

public class OsrAndCountedLoop {

    public static void main(String[] args) throws InterruptedException {
        // on JDK 11 it's no longer an issue: C2 compiles doSomethingCounted
        // placing a SAFEPOINT POLL on the counted loop, treating it as non-counted

        //please run me with:
        //-XX:+PrintCompilation -XX:+PrintGCApplicationStoppedTime -XX:+PrintSafepointStatistics -XX:-UseBiasedLocking -XX:-UseCounterDecay
        final boolean OSR = false;
        if (!OSR) {
            //JDK8 Compiler threshold policy (tiered compilation):
            //- http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/file/2b2511bd3cc8/src/share/vm/runtime/advancedThresholdPolicy.hpp#l34
            //Cliff Click on OSR and warmup code:
            //- http://cliffc.org/blog/2011/11/22/what-the-heck-is-osr-and-why-is-it-bad-or-good/
            //J. Bempel on how compiler threshold works:
            //http://jpbempel.blogspot.com/2013/04/compilethreshold-is-relative.html
            for (int i = 0; i < 100000; i++) {
                doSomethingCounted(10);
            }
        }
        Thread t = new Thread(() -> {
            //if !OSR the {poll_return} will "never" be hit, hanging the JVM
            //uncapable to reach a safepoint
            long l = doSomethingCounted(Integer.MAX_VALUE);
            System.out.println("I swear I'm not dead code! " + l);
        });
        t.setDaemon(true);
        t.start();
        //just to have some more fun: GC will EVER happen? :)
        System.gc();
        Thread.sleep(5000);
    }

    //If not warmed up OSR C2/Level 4 will compile the method on Thread::run as an uncounted loop(s),
    //by adding safepoint {poll}s that allow it to be interrupted.
    //
    //If warmed up, C2/Level 4 will compile it as a counted loop
    //that will not be inlined on Thread::run (that's interpreted/cold).
    //Without being inlined, it will contains just a {poll_return} on method exit.
    private static long doSomethingCounted(int limit) {
        long k = 0;
        for (int l = 0; l < limit; l++) {
            for (int i = 0; i < limit; i++) {
                for (int j = 0; j < 2; j++) {
                    k++;
                    if ((k % 2) == 1)
                        k += l;
                }
            }
        }
        return k;
    }


}
