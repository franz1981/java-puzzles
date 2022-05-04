package red.hat.puzzles.polymorphism;

import java.lang.ref.WeakReference;
import java.util.function.ToIntFunction;

public class SwitchClassUtil {

    private static WeakReference<Class<?>>[] createWeakRefArray(Class<?>[] types) {
        @SuppressWarnings("unchecked")
        WeakReference<Class<?>>[] refs = (WeakReference<Class<?>>[]) new WeakReference<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            refs[i] = new WeakReference<>(types[i]);
        }
        return refs;
    }

    public static ToIntFunction<Object> createSwitchOf(Class<?>... types) {
        final int NO_MATCH = -2;
        final int NULL_MATCH = -2;
        WeakReference<Class<?>>[] refs = createWeakRefArray(types);
        return instance -> {
            if (instance == null) {
                return NULL_MATCH;
            }
            for (int i = 0; i < refs.length; i++) {
                Class<?> typecase = refs[i].get();
                if (typecase != null && typecase.isInstance(instance)) {
                    return i;
                }
            }
            return NO_MATCH;
        };
    }

    public static ClassValue<Integer> createClassValueSwitchOf(Class<?>... types) {
        final int NO_MATCH = -2;
        final int NULL_MATCH = -2;
        class MutableInt {
            Integer value;
        }
        MutableInt index = new MutableInt();

        final ClassValue<Integer> classCache = new ClassValue<Integer>() {
            @Override
            protected Integer computeValue(Class<?> type) {
                return index.value;
            }
        };
        for (int i = 0; i < types.length; i++) {
            index.value = i;
            classCache.get(types[i]);
        }
        index.value = NO_MATCH;
        return classCache;
    }
}
