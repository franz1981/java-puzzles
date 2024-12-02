package red.hat.puzzles.cstatic;

public class ArrayInitGood {

    public final static byte[] TABLE = Helper.prepareTable();


    private static class Helper {

        static byte[] prepareTable() {
            byte[] table = new byte[1000_000_000];
            table[0] = 0;
            for (int i = 1; i < table.length; i++) {
                table[i] = nextValue(i);
            }
            return table;
        }

        private static byte nextValue(int i) {
            return (byte) (i % 128);
        }

    }
}
