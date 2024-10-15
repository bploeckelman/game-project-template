package lando.systems.game.utils;

public class Direction {

    public enum Rotation { CW, CCW }
    public enum Relative { UP, DOWN, LEFT, RIGHT }
    public enum Movement { FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN }
    public enum Cardinal { NORTH, SOUTH, EAST, WEST }
    public enum Compass  {
        NORTH, NORTH_EAST,
        EAST, SOUTH_EAST,
        SOUTH, SOUTH_WEST,
        WEST, NORTH_WEST
    }
    public enum Angle {
        DEG_0, DEG_45, DEG_90,
        DEG_135, DEG_180, DEG_225,
        DEG_270, DEG_315, DEG_360
    }
}
