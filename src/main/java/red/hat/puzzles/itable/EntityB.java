package red.hat.puzzles.itable;

public class EntityB implements IdPresence {

    private int id;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
