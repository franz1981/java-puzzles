package red.hat.puzzles.concurrent;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
public class LockCoarsening {

    private static class SyncHolder {

        private Object obj1;
        private Object obj2;
        private Object obj3;
        private final Object lock = new Object();


        public void setObj1(Object obj1) {
            synchronized (this) {
                this.obj1 = obj1;
            }
        }

        public void setObj2(Object obj2) {
            synchronized (this) {
                this.obj2 = obj2;
            }
        }

        public void setObj3(Object obj3) {
            synchronized (this) {
                this.obj3 = obj3;
            }
        }

        public void setObj1ExternalLock(Object obj1) {
            synchronized (lock) {
                this.obj1 = obj1;
            }
        }

        public void setObj2ExternalLock(Object obj2) {
            synchronized (lock) {
                this.obj2 = obj2;
            }
        }

        public void setObj3ExternalLock(Object obj3) {
            synchronized (lock) {
                this.obj3 = obj3;
            }
        }

    }

    private SyncHolder holder;
    private Object objValue;
    @Setup
    public void init() {
        objValue = new Object();
        holder = new SyncHolder();
    }

    /**
     * reentrant fast-locking seems to behave differently if using a local variable vs an instance field
     *
     * Running on JDK 17:
     *
     * Benchmark                          Mode  Cnt    Score   Error   Units
     * LockCoarsening.notReentrantField  thrpt   10  242.901 ? 0.780  ops/us
     * LockCoarsening.reentrantField     thrpt   10  142.707 ? 2.195  ops/us
     * LockCoarsening.reentrantLocal     thrpt   10   39.118 ? 1.999  ops/us
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object notReentrantField() {
        synchronized (objValue) {
            return objValue;
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object reentrantField() {
        synchronized (objValue) {
            synchronized (objValue) {
                return objValue;
            }
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object reentrantLocal() {
        var lock = objValue;
        synchronized (lock) {
            synchronized (lock) {
                return objValue;
            }
        }
    }

    /**
     * The setObjsOnHolder shows that the synchronized blocks are merged to become a single one,
     * if value is saved in a local variable vs read it again from an instance field
     *
     * Benchmark                                                        Mode  Cnt    Score   Error   Units
     * LockCoarsening.setObjsOnHolderInInstanceField                   thrpt   10   93.529 ± 0.453  ops/us
     * LockCoarsening.setObjsOnHolderInInstanceFieldUsingExternalLock  thrpt   10   91.665 ± 1.216  ops/us
     * LockCoarsening.setObjsOnHolderInLocalField                      thrpt   10  248.097 ± 3.917  ops/us
     * LockCoarsening.setObjsOnHolderInLocalFieldUsingExternalLock     thrpt   10   83.625 ± 0.441  ops/us
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void setObjsOnHolderInInstanceField() {
        var value = this.objValue;
        holder.setObj1(value);
        holder.setObj2(value);
        holder.setObj3(value);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void setObjsOnHolderInLocalField() {
        var value = this.objValue;
        var holder = this.holder;
        holder.setObj1(value);
        holder.setObj2(value);
        holder.setObj3(value);
    }

    /**
     * Similar to the previous 2 cases, but show that synchronized regions are not merged if the lock obj isn't this -
     * which is considered a trusted and stable value, while a final lock field, not.
     */
    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void setObjsOnHolderInInstanceFieldUsingExternalLock() {
        var value = this.objValue;
        holder.setObj1ExternalLock(value);
        holder.setObj2ExternalLock(value);
        holder.setObj3ExternalLock(value);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void setObjsOnHolderInLocalFieldUsingExternalLock() {
        var value = this.objValue;
        var holder = this.holder;
        holder.setObj1ExternalLock(value);
        holder.setObj2ExternalLock(value);
        holder.setObj3ExternalLock(value);
    }

    /**
     *
     * This part of the banchmark mimic creation of a fresh new holder and setting the obj fields.
     *
     * Benchmark                                              Mode  Cnt    Score   Error   Units
     * LockCoarsening.createHolderAndSetObjs                 thrpt   10   67.558 ± 0.471  ops/us
     * LockCoarsening.createHolderInLocalFieldAndSetObjs     thrpt   10  119.812 ± 1.485  ops/us
     * LockCoarsening.createHolderInlinedInLocalsAndSetObjs  thrpt   10  119.136 ± 0.485  ops/us
     */

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public SyncHolder createHolderInlinedInLocalsAndSetObjs() {
        var objValue = this.objValue;
        synchronized (objValue) {
            var holder = this.holder;
            if (holder != null) {
                SyncHolder newHolder = new SyncHolder();
                this.holder = newHolder;
                newHolder.setObj1(objValue);
                newHolder.setObj2(objValue);
                newHolder.setObj3(objValue);
                return newHolder;
            }
        }
        System.exit(1);
        return null;
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public SyncHolder createHolderInLocalFieldAndSetObjs() {
        var objValue = this.objValue;
        synchronized (objValue) {
            var holder = this.holder;
            if (holder != null) {
                SyncHolder localHolder = createAndSetNewHolder(objValue);
                localHolder.setObj3(objValue);
                return localHolder;
            }
        }
        System.exit(1);
        return null;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public SyncHolder createHolderAndSetObjs() {
        var objValue = this.objValue;
        synchronized (objValue) {
            if (holder != null) {
                holder = new SyncHolder();
                holder.setObj1(objValue);
                holder.setObj2(objValue);
                holder.setObj3(objValue);
                return holder;
            }
        }
        System.exit(1);
        return null;
    }



    private SyncHolder createAndSetNewHolder(Object objValue) {
        SyncHolder newHolder = new SyncHolder();
        this.holder = newHolder;
        newHolder.setObj1(objValue);
        newHolder.setObj2(objValue);
        return newHolder;
    }

}
