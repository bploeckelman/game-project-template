package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.World;
import lando.systems.game.utils.Util;

import java.util.EnumSet;

public class Collider extends Component {

    private static final String TAG = Collider.class.getSimpleName();

    public enum Shape {rect, circ, grid}

    public enum Mask {solid, npc, effect, object}

    public final Shape shape;
    public final Mask mask;

    // Shape data -------------------------------------------------------------
    // NOTE: only one non-null depending which factory method was used, corresponds to 'Shape'

    public final Rectangle rect;
    public final Circle circ;
    public final Grid grid;

    // Custom shape types -----------------------------------------------------

    public static class Grid {
        public int tileSize;
        public int cols;
        public int rows;
        public Tile[] tiles;

        public Grid(int tileSize, int cols, int rows) {
            this.tileSize = tileSize;
            this.cols = cols;
            this.rows = rows;
            this.tiles = new Tile[cols * rows];
            for (int i = 0; i < cols * rows; i++) {
                tiles[i] = new Tile();
            }
        }

        // TODO(brian): better to use 'int value' for flexibility, can still check zero/non-zero
        public static class Tile {
            public boolean state;
        }
    }

    // Factory methods --------------------------------------------------------

    public static Collider makeRect(Mask mask, float x, float y, float w, float h) {
        return new Collider(mask, x, y, w, h);
    }

    public static Collider makeCirc(Mask mask, float x, float y, float r) {
        return new Collider(mask, x, y, r);
    }

    private static Collider makeGrid(Mask mask, int tileSize, int cols, int rows) {
        return new Collider(mask, tileSize, cols, rows);
    }

    // Private constructors ---------------------------------------------------

    private Collider(Mask mask, float x, float y, float w, float h) {
        this.shape = Shape.rect;
        this.mask = mask;
        this.rect = new Rectangle(x, y, w, h);
        this.circ = null;
        this.grid = null;
    }

    private Collider(Mask mask, float x, float y, float r) {
        this.shape = Shape.circ;
        this.mask = mask;
        this.rect = null;
        this.circ = new Circle(x, y, r);
        this.grid = null;
    }

    private Collider(Mask mask, int tileSize, int cols, int rows) {
        this.shape = Shape.grid;
        this.mask = mask;
        this.rect = null;
        this.circ = null;
        this.grid = new Grid(tileSize, cols, rows);
    }

    public void gridSet(int x, int y, boolean state) {
        if (grid == null) {
            Util.log(TAG, "Collider.gridSet(%d, %d, %b) called on non-grid, ignored"
                .formatted(x, y, state));
            return;
        }

        if (!Calc.between(x, 0, grid.cols - 1)
            || !Calc.between(y, 0, grid.rows - 1)) {
            Util.log(TAG, "Collider.gridSet(%d, %d, %b) called with out of bounds coords, ignored"
                .formatted(x, y, state));
            return;
        }

        var index = x + y * grid.cols;
        grid.tiles[index].state = state;
    }

    // Implementation ---------------------------------------------------------
    // NOTE(brian): there's room for improvement in the API here,
    //  the addition of the Mover.collidesWith mask set should be the default way of running `check*()` family methods
    //  but we still want to be able to arbitrarily run checks on a Collider against a specific Mask type,
    //  so for now I'm leaving the `check*(Mask mask, ...)` overrides and just updating the one used by Mover

    public Collider checkAndGet(EnumSet<Mask> masks, int xOffset, int yOffset) {
        var colliders = World.components.getComponents(Collider.class);
        for (var collider : colliders) {
            if (collider == this) continue;
            if (collider.notActive()) continue;
            if (!masks.contains(collider.mask)) continue;

            if (this.overlaps(collider, xOffset, yOffset)) {
                return collider;
            }
        }
        return null;
    }

    public boolean check(Mask mask) {
        return check(mask, 0, 0);
    }

    public boolean check(Mask mask, int xOffset, int yOffset) {
        var hit = checkAndGet(mask, xOffset, yOffset);
        return (hit != null);
    }

    public Collider checkAndGet(Mask mask) {
        return checkAndGet(mask, 0, 0);
    }

    public Collider checkAndGet(Mask mask, int xOffset, int yOffset) {
        var colliders = World.components.getComponents(Collider.class);
        for (var collider : colliders) {
            if (collider == this) continue;
            if (collider.notActive()) continue;
            if (collider.mask != mask) continue;

            if (this.overlaps(collider, xOffset, yOffset)) {
                return collider;
            }
        }
        return null;
    }

    public boolean overlaps(Collider other, int xOffset, int yOffset) {
        return switch (shape) {
            case rect -> switch (other.shape) {
                case rect -> overlapsRectRect(this, other, xOffset, yOffset);
                case circ -> overlapsRectCirc(this, other, xOffset, yOffset);
                case grid -> overlapsRectGrid(this, other, xOffset, yOffset);
            };
            case circ -> switch (other.shape) {
                case rect -> overlapsRectCirc(other, this, xOffset, yOffset);
                case circ -> overlapsCircCirc(this, other, xOffset, yOffset);
                case grid -> overlapsCircGrid(this, other, xOffset, yOffset);
            };
            case grid -> switch (other.shape) {
                case rect -> overlapsRectGrid(other, this, xOffset, yOffset);
                case circ -> overlapsCircGrid(other, this, xOffset, yOffset);
                case grid -> {
                    Util.log(TAG, "unsupported overlap check: grid/grid");
                    yield false;
                }
            };
        };
    }

    public boolean overlapsRectRect(Collider a, Collider b, int xOffset, int yOffset) {
        var aRect = Util.rect.obtain().set(0, 0, 0, 0);
        var bRect = Util.rect.obtain().set(0, 0, 0, 0);
        var aPos = Util.vec2.obtain().setZero();
        var bPos = Util.vec2.obtain().setZero();

        var aPosition = a.entity.get(Position.class);
        var bPosition = b.entity.get(Position.class);
        if (aPosition != null && aPosition.active) aPos.set(aPosition.value);
        if (bPosition != null && bPosition.active) bPos.set(bPosition.value);

        aRect.set(
            a.rect.x + aPos.x + xOffset,
            a.rect.y + aPos.y + yOffset,
            a.rect.width, a.rect.height);

        // TODO(brian): need a way to indicate which (a or b) the offsets should apply to
        //  so `overlapsRectCirc` behaves correctly when this.circ and that.rect
        //  and we call it as: `overlapsRectCirc(other, this, ...)`
        bRect.set(
            b.rect.x + bPos.x,
            b.rect.y + bPos.y,
            b.rect.width, b.rect.height);

        var overlaps = aRect.overlaps(bRect);
        Util.free(aPos, bPos);
        Util.free(aRect, bRect);
        return overlaps;
    }

    public boolean overlapsRectCirc(Collider r, Collider c, int xOffset, int yOffset) {
        // obtain temp objects to work with
        var rect = Util.rect.obtain();
        var circ = Util.circ.obtain();
        var aPos  = Util.vec2.obtain().setZero();
        var bPos  = Util.vec2.obtain().setZero();

        // use active position components attached to each entity, if any
        var aPosition = r.entity.getIfActive(Position.class);
        var bPosition = c.entity.getIfActive(Position.class);
        if (aPosition != null) aPos.set(aPosition.value);
        if (bPosition != null) bPos.set(bPosition.value);

        // TODO(brian): need a way to indicate which (a or b) the offsets should apply to
        //  so `overlapsRectCirc` behaves correctly when this.circ and that.rect
        //  and we call it as: `overlapsRectCirc(other, this, ...)`
        rect.set(
            r.rect.x + aPos.x,
            r.rect.y + aPos.y,
            r.rect.width, r.rect.height);

        // TODO(brian): this is a good example for the above comment:
        //  we should be able to ignore which collider is moving and which is stationary,
        //  but if the offsets are added to the rect in the case where the test rects are stationary
        //  and the circles are moving, then the circles get 'stuck' in the rect on collision,
        //  but if the offsets are added to the circles, then it works as expected
        circ.set(
            c.circ.x + bPos.x + xOffset,
            c.circ.y + bPos.y + yOffset,
            c.circ.radius);

        // do they overlap?
        var overlaps = Intersector.overlaps(circ, rect);

        // cleanup and return
        Util.vec2.free(bPos);
        Util.vec2.free(aPos);
        Util.circ.free(circ);
        Util.rect.free(rect);
        return overlaps;
    }

    public boolean overlapsRectGrid(Collider r, Collider g, int xOffset, int yOffset) {
        if (r.rect == null || g.grid == null) {
            Util.log(TAG, "overlapsRectGrid called for colliders with the wrong shapes");
            return false;
        }
        var rows = g.grid.rows;
        var cols = g.grid.cols;
        var tileSize = g.grid.tileSize;

        // obtain temp objects to work with
        var rect = Util.rect.obtain().set(0, 0, 0, 0);
        var grid = Util.rect.obtain().set(0, 0, 0, 0);
        var rPos = Util.vec2.obtain().setZero();
        var gPos = Util.vec2.obtain().setZero();

        // use active position components attached to each entity, if any
        var rPosition = r.entity.get(Position.class);
        var gPosition = g.entity.get(Position.class);
        if (rPosition != null && rPosition.active) rPos.set(rPosition.value);
        if (gPosition != null && gPosition.active) gPos.set(gPosition.value);

        // construct the rectangle relative to the grid
        rect.set(
            r.rect.x + rPos.x + xOffset - gPos.x,
            r.rect.y + rPos.y + yOffset - gPos.y,
            r.rect.width, r.rect.height);

        // construct the rectangle describing the boundary of the grid
        grid.set(gPos.x, gPos.y, cols * tileSize, rows * tileSize);

        // only worth checking against the grid tiles if the rectangle is within the grid bounds
        var overlaps = false;
        if (rect.overlaps(grid)) {
            // get the range of grid tiles that the rectangle overlaps on each axis
            int left   = Calc.clampInt((int) Calc.floor(rect.x                   / (float) tileSize), 0, cols);
            int right  = Calc.clampInt((int) Calc.ceiling((rect.x + rect.width)  / (float) tileSize), 0, cols);
            int top    = Calc.clampInt((int) Calc.ceiling((rect.y + rect.height) / (float) tileSize), 0, rows);
            int bottom = Calc.clampInt((int) Calc.floor(rect.y                   / (float) tileSize), 0, rows);

            // check each tile in the possible overlap range for solidity
            for (int y = bottom; y < top; y++) {
                for (int x = left; x < right; x++) {
                    var i = x + y * cols;
                    var solid = g.grid.tiles[i].state;
                    if (solid) {
                        overlaps = true;
                        break;
                    }
                }
            }
        }

        Util.vec2.free(gPos);
        Util.vec2.free(rPos);
        Util.rect.free(grid);
        Util.rect.free(rect);
        return overlaps;
    }

    public boolean overlapsCircCirc(Collider a, Collider b, int xOffset, int yOffset) {
        // obtain temp objects to work with
        var aCirc = Util.circ.obtain();
        var bCirc = Util.circ.obtain();
        var aPos  = Util.vec2.obtain().setZero();
        var bPos  = Util.vec2.obtain().setZero();

        // use active position components attached to each entity, if any
        var aPosition = a.entity.getIfActive(Position.class);
        var bPosition = b.entity.getIfActive(Position.class);
        if (aPosition != null) aPos.set(aPosition.value);
        if (bPosition != null) bPos.set(bPosition.value);

        // construct the source circle relative to the target circle
//        aCirc.set(
//            a.circ.x + aPos.x + xOffset - bPos.x,
//            a.circ.y + aPos.y + yOffset - bPos.y,
//            a.circ.radius);

        aCirc.set(aPos.x + xOffset, aPos.y + yOffset, a.circ.radius);
        bCirc.set(bPos.x, bPos.y, b.circ.radius);

        // do they overlap?
        var overlaps = aCirc.overlaps(bCirc);

        // cleanup and return
        Util.vec2.free(bPos);
        Util.vec2.free(aPos);
        Util.circ.free(bCirc);
        Util.circ.free(aCirc);
        return overlaps;
    }

    public boolean overlapsCircGrid(Collider c, Collider g, int xOffset, int yOffset) {
        var circ = c.circ;
        var grid = g.circ;
        // TODO...
        return false;
    }
}
