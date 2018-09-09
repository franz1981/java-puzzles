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

package red.hat.puzzles.combiner;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public final class VanillaCombiner implements Combiner {

    private static final class Node {
        public Runnable request;
        public final AtomicInteger wait = new AtomicInteger();
        public boolean complete;
        public final AtomicReference<Node> next = new AtomicReference<Node>();
    }

    private ThreadLocal<Node> _myNode = new ThreadLocal<Node>() {
        @Override
        protected Node initialValue() {
            return new Node();
        }
    };

    private final int _limit;
    private final AtomicReference<Node> _tail;

    public VanillaCombiner(int limit) {
        _limit = limit;
        _tail = new AtomicReference<Node>(new Node());
    }

    public void combine(Runnable action, Combiner.IdleStrategy idleStrategy) {
        Node nextNode = _myNode.get();
        nextNode.complete = false;
        nextNode.wait.set(1);

        Node curNode = _tail.getAndSet(nextNode);
        _myNode.set(curNode);

        //
        // There's now a window where nextNode/_tail can't be reached.
        // So, any communication has to be done via the previous node
        // in the list, curNode.
        //

        curNode.request = action;
        curNode.next.lazySet(nextNode);

        // Wait until our request has been fulfilled or we are the combiner.

        int idleCount = 0;
        while (curNode.wait.get() == 1) {
            idleCount = idleStrategy.idle(idleCount);
        }

        if (curNode.complete)
            return;

        // We are now the combiner. We copy n's Next field into nn, as n will
        // become untouchable after n.wait.lazySet(0), due to reuse.
        Node n = curNode;
        Node nn;
        for (int c = 0; c < _limit && (nn = n.next.get()) != null; ++c, n = nn) {
            n.request.run();

            n.next.set(null);
            n.request = null;
            n.complete = true;
            n.wait.lazySet(0);
        }

        // Make someone else the combiner.
        n.wait.set(0);
    }
}