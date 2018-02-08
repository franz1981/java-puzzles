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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
public class MonomorphicCallSiteBenchmark {


    public interface IdentityTrait {
        default int getDefaultId() {
            return -1;
        }

        int getId();

    }

    public static final class MessageWithIdentity implements IdentityTrait {
        private int id;

        MessageWithIdentity(int id) {
            this.id = id;
        }

        @Override
        public int getDefaultId() {
            return id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    public static abstract class Identifiable {

        protected final int id;

        Identifiable(int id) {
            this.id = id;
        }

        public final int getId() {
            return id;
        }

        public abstract int getAbstractId();
    }

    public static final class IdentifiableMessage extends Identifiable {

        IdentifiableMessage(int id) {
            super(id);
        }

        @Override
        public int getAbstractId() {
            return id;
        }
    }

    private IdentityTrait identityTrait;
    private Identifiable identifiable;

    @Setup
    public void init() {
        identifiable = new IdentifiableMessage(10);
        identityTrait = new MessageWithIdentity(10);
    }

    @Benchmark
    public int getDefaultId() {
        return identityTrait.getDefaultId();
    }

    @Benchmark
    public int getInterfaceId() {
        return identityTrait.getId();
    }

    @Benchmark
    public int getAbstractId() {
        return identifiable.getAbstractId();
    }

    @Benchmark
    public int getId() {
        return identifiable.getId();
    }

    public static void main(String[] args) throws RunnerException {
        ArrayFillBenchmark.runBenchmark(MonomorphicCallSiteBenchmark.class);
    }

}
