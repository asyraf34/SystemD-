import java.util.ArrayList;
import java.util.List;

/**
 * Stores and provides all level map data and dimensions.
 */
public class GameMap {

    private final List<String[]> levelMaps;
    private final int rowCount;
    private final int columnCount;

    private static final String[] tileMapLevel3 = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X  XXX",
            "O       bpo       O",
            "XXXX X XX XX   XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    private static final String[] tileMapLevel2 = {
            "XXXXXXXXXXXXXXXXXXX",
            "X       b       X X",
            "X P X XXXXX X   X X",
            "X   X X   X X   X X",
            "XX XX       XXrXX X",
            "X   X XXXXX X   X X",
            "X X X   X   X X X X",
            "X X XXX X XXX X X X",
            "X X X   o   X X X X",
            "X   X       X     X",
            "XXXXX X   X XXXXX X",
            "O     XXXXX     O X",
            "XXXXX X   X XXXXX X",
            "X   X XX XX X   X X",
            "X X X       X X X X",
            "X X XXXX XXXX X X X",
            "X X X   p   X X X X",
            "X   X XX XX X   X X",
            "X XXX X   X XXX X X",
            "X     X   X     XpX",
            "XXXXXXXXXXXXXXXXXXX"
    };

    private static final String[] tileMapLevel1 = {
            "XXXXXXXXXXXXXXXXXXX",
            "X P               X",
            "X X XXXXXXXXXXX X X",
            "X                 X",
            "X X             X X",
            "X                 X",
            "X X             X X",
            "X                 X",
            "X X             X X",
            "X        B        X",
            "X X             X X",
            "X                 X",
            "X X             X X",
            "X                 X",
            "X X             X X",
            "X                 X",
            "X X XXXXXXXXXXX X X",
            "X                 X",
            "X XXXXXXXXXXXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    public GameMap() {
        levelMaps = new ArrayList<>();
        levelMaps.add(tileMapLevel1);
        levelMaps.add(tileMapLevel2);
        levelMaps.add(tileMapLevel3);

        // Assuming all maps are the same size
        this.rowCount = tileMapLevel1.length;
        this.columnCount = tileMapLevel1[0].length();
    }

    public String[] getMapData(int level) {
        if (level < 1 || level > levelMaps.size()) {
            // Handle error or return a default, e.g., level 1
            return levelMaps.get(1);
        }
        return levelMaps.get(level - 1);
    }

    public int getLevelCount() {
        return levelMaps.size();
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }
}