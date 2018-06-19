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

package red.hat.puzzles.benchmarks.qpid;

import org.apache.qpid.jms.provider.ProviderFuture;
import org.apache.qpid.jms.provider.ProviderSynchronization;
import org.apache.qpid.jms.util.IOExceptionSupport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SpinningProviderFuture extends ProviderFuture {

    public SpinningProviderFuture() {
        this(null);
    }

    public SpinningProviderFuture(ProviderSynchronization synchronization) {
        super(synchronization);
    }

    /**
     * Waits for a response to some Provider requested operation.
     *
     * @throws IOException if an error occurs while waiting for the response.
     */
    public void sync() throws IOException {
        try {
            while (true) {
                if (isComplete()) {
                    failOnError();
                    return;
                }
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw IOExceptionSupport.create(e);
        }
    }

    /**
     * Timed wait for a response to a Provider operation.
     *
     * @param amount The amount of time to wait before abandoning the wait.
     * @param unit   The unit to use for this wait period.
     * @return true if the operation succeeded and false if the waiting time elapsed while
     * waiting for the operation to complete.
     * @throws IOException if an error occurs while waiting for the response.
     */
    public boolean sync(long amount, TimeUnit unit) throws IOException {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

}
