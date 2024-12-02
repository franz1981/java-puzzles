package red.hat.puzzles.cstatic;

public class ArrayInitBad {

    public final static byte[] TABLE = new byte[1000_000_000];

    static {
        prepareTable();
    }

    private static void prepareTable() {
        TABLE[0] = 0;
        for (int i = 1; i < TABLE.length; i++) {
            calcNextValue(i);
        }
    }

    private static void calcNextValue(int i) {
        TABLE[i] = (byte) (i % 128);
    }
}
