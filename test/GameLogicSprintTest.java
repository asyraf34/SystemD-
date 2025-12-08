import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MafiaSprint {
    boolean isSprinting = false;
    int sprintTicksRemaining = 0;
    int speed = 2;
}

class GameStateTest {
    MafiaSprint sprint = new MafiaSprint();
}

class GameLogic {
    // Example logic for reference; adapt if needed!
    void updateSprintState(GameStateTest state) {
        if (state.sprint.isSprinting) {
            state.sprint.sprintTicksRemaining--;
            if (state.sprint.sprintTicksRemaining <= 0) {
                state.sprint.isSprinting = false;
                state.sprint.speed = 2; // reset to normal speed
            }
        }
    }
}

public class GameLogicSprintTest {
    GameLogic logic;
    GameStateTest state;

    @BeforeEach
    void setup() {
        logic = new GameLogic();
        state = new GameStateTest();
    }

    @Test
    void testSprintTickDecrements() {
        state.sprint.isSprinting = true;
        state.sprint.sprintTicksRemaining = 5;
        state.sprint.speed = 4; // sprint speed

        logic.updateSprintState(state);

        assertEquals(4, state.sprint.sprintTicksRemaining, "Sprint ticks should decrement");
        assertTrue(state.sprint.isSprinting, "Sprint should still be active");
        assertEquals(4, state.sprint.speed, "Should still have sprint speed");
    }

    @Test
    void testSprintEndsAtZero() {
        state.sprint.isSprinting = true;
        state.sprint.sprintTicksRemaining = 1;
        state.sprint.speed = 4; // sprint speed

        logic.updateSprintState(state);

        assertEquals(0, state.sprint.sprintTicksRemaining, "Sprint ticks should be zero");
        assertFalse(state.sprint.isSprinting, "Sprint should end");
        assertEquals(2, state.sprint.speed, "Speed should reset after sprint ends");
    }

    @Test
    void testNoSprintNoChange() {
        state.sprint.isSprinting = false;
        state.sprint.sprintTicksRemaining = 0;
        int oldSpeed = state.sprint.speed;

        logic.updateSprintState(state);

        assertEquals(0, state.sprint.sprintTicksRemaining, "Sprint ticks unchanged");
        assertFalse(state.sprint.isSprinting, "Sprint not active");
        assertEquals(oldSpeed, state.sprint.speed, "Speed unchanged");
    }
}