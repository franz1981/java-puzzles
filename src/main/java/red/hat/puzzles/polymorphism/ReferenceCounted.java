package red.hat.puzzles.polymorphism;

public interface ReferenceCounted {

    void release();

    static void release(Object o) {
        if (o instanceof ReferenceCounted) {
            ((ReferenceCounted) o).release();
        }
    }
}
