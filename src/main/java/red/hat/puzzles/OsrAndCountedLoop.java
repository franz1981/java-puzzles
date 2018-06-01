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

package red.hat.puzzles;

public class OsrAndCountedLoop {

    public static void main(String[] args) throws InterruptedException {
        //please run me with: -XX:+PrintCompilation -XX:+PrintGCApplicationStoppedTime
        final boolean justOSR = false;
        if (!justOSR) {
            for (int i = 0; i < 100000; i++) {
                doSomethingCounted(10);
            }
        }
        Thread t = new Thread(() -> {
            //if !justOSR it will call the C2 compiled version without safepoint polls
            long l = doSomethingCounted(Integer.MAX_VALUE);
            System.out.println("I swear I'm not dead code! " + l);
        });
        t.setDaemon(true);
        t.start();
        //just to have some fun: GC will EVER happen? :)
        System.gc();
        Thread.sleep(5000);
    }

    //OSR will treat it as a non-counted loop:
    //"for (int j = 0; j < 2; j++)" block will be compiled (with a {poll}) on OSR
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
