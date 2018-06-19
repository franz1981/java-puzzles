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

package red.hat.puzzles.benchmarks;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Asynchronous Provider Future class.
 */
public class ProviderFuture implements AsyncResult {

    private static final AtomicIntegerFieldUpdater<ProviderFuture> COMPLETE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ProviderFuture.class, "complete");

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile Throwable error;

    @SuppressWarnings("unused")
    private volatile int complete;

    public ProviderFuture() {
    }

    @Override
    public boolean isComplete() {
        return latch.getCount() == 0;
    }

    @Override
    public void onFailure(Throwable result) {
        if (COMPLETE_UPDATER.compareAndSet(this, 0, 1)) {
            error = result;
            latch.countDown();
        }
    }

    @Override
    public void onSuccess() {
        if (COMPLETE_UPDATER.compareAndSet(this, 0, 1)) {
            latch.countDown();
        }
    }

    /**
     * Timed wait for a response to a Provider operation.
     *
     * @param amount
     *        The amount of time to wait before abandoning the wait.
     * @param unit
     *        The unit to use for this wait period.
     *
     * @return true if the operation succeeded and false if the waiting time elapsed while
     * 	       waiting for the operation to complete.
     *
     * @throws IOException if an error occurs while waiting for the response.
     */
    @Override
    public boolean sync(long amount, TimeUnit unit) throws IOException {
        boolean result = false;
        try {
            result = latch.await(amount, unit);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw IOExceptionSupport.create(e);
        }
        failOnError();

        return result;
    }

    /**
     * Waits for a response to some Provider requested operation.
     *
     * @throws IOException if an error occurs while waiting for the response.
     */
    @Override
    public void sync() throws IOException {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw IOExceptionSupport.create(e);
        }
        failOnError();
    }

    private void failOnError() throws IOException {
        Throwable cause = error;
        if (cause != null) {
            throw IOExceptionSupport.create(cause);
        }
    }
}
