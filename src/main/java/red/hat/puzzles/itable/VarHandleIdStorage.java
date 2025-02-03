package red.hat.puzzles.itable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleIdStorage {

    private static final ClassValue<VarHandle> classValue = new ClassValue<>() {
        @Override
        protected VarHandle computeValue(Class<?> type) {
            assert IdPresence.class.isAssignableFrom(type);
            // use method handles to access the field "id" of the class
            try {
                return MethodHandles.privateLookupIn(type, MethodHandles.lookup())
                        .findVarHandle(type, "id", int.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    };

    public static void setId(IdPresence idPresence, int id) {
        classValue.get(idPresence.getClass()).set(idPresence, id);
    }

    public static int getId(IdPresence idPresence) {
        return (int) classValue.get(idPresence.getClass()).get(idPresence);
    }
}
