import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest2 {
    boolean gameWon = false;
    boolean gameOver = false;

    void winGame() {
        gameWon = true;
        gameOver = false; // Game can’t be both won & over at once
    }

    void endGame() {
        gameOver = true;
        gameWon = false;
    }

    void reset() {
        gameWon = false;
        gameOver = false;
    }
}

public class GameStateWinLoseTest {
    GameStateTest2 state;

    @BeforeEach
    void setup() {
        state = new GameStateTest2();
    }

    @Test
    void testWinGame() {
        state.winGame();
        assertTrue(state.gameWon, "Game should be won");
        assertFalse(state.gameOver, "Game over should be false after win");
    }

    @Test
    void testEndGame() {
        state.endGame();
        assertTrue(state.gameOver, "Game should be over after loss");
        assertFalse(state.gameWon, "Game won should be false after game over");
    }

    @Test
    void testResetClearsState() {
        state.winGame();
        state.reset();
        assertFalse(state.gameWon, "Game won should be reset");
        assertFalse(state.gameOver, "Game over should be reset");

        state.endGame();
        state.reset();
        assertFalse(state.gameWon, "Game won should be reset");
        assertFalse(state.gameOver, "Game over should be reset");
    }
}