package red.hat.puzzles.itable;

import sun.misc.Unsafe;

public class UnsafeIdStorage {

    // similar to VarHandleIdStorage but return the id offset and use unsafe to get and set the id
    private static final ClassValue<Long> classValue = new ClassValue<>() {
        @Override
        protected Long computeValue(Class<?> type) {
            assert IdPresence.class.isAssignableFrom(type);
            // NOTE: it doesn't work with records or hidden classes!
            try {
                return UNSAFE.objectFieldOffset(type.getDeclaredField("id"));
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        }
    };

    private static final Unsafe UNSAFE;

    static {
        // retrieve the unsafe instance
        try {
            var field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void setId(IdPresence idPresence, int id) {
        UNSAFE.putInt(idPresence, classValue.get(idPresence.getClass()), id);
    }

    public static int getId(IdPresence idPresence) {
        return UNSAFE.getInt(idPresence, classValue.get(idPresence.getClass()));
    }


}
