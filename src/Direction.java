public enum Direction {
    UP, DOWN, LEFT, RIGHT, NONE;

    // Helper to get X/Y changes
    public int getDx(int speed) {
        switch (this) {
            case LEFT: return -speed;
            case RIGHT: return speed;
            default: return 0;
        }
    }
    public int getDy(int speed) {
        switch (this) {
            case UP: return -speed;
            case DOWN: return speed;
            default: return 0;
        }
    }
}