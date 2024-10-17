package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.World;
import lando.systems.game.utils.Util;

public class Collider extends Component {

    private static final String TAG = Collider.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Collider.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public enum Shape { rect, circ, grid }

    public enum Mask { solid }

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
            this.tiles = new Tile[cols*rows];
            for (int i = 0; i < cols*rows; i++) {
                tiles[i] = new Tile();
            }
        }

        // TODO(brian): better to use 'int value' for flexibility, can still check zero/non-zero
        public record Tile(boolean state) {
            public Tile() {
                this(false);
            }
        }
    }

    // Factory methods --------------------------------------------------------

    public static Collider makeRect(Mask mask, float x, float y, float w, float h) {
        return new Collider(mask, x, y, w, h);
    }

    private static Collider makeCirc(Mask mask, float x, float y, float r) {
        return new Collider(mask, x, y, r);
    }

    private static Collider makeGrid(Mask mask, int tileSize, int cols, int rows) {
        return new Collider(mask, tileSize, cols, rows);
    }

    // Private constructors ---------------------------------------------------

    private Collider(Mask mask, float x, float y, float w, float h) {
        super(type);
        this.shape = Shape.rect;
        this.mask = mask;
        this.rect = new Rectangle(x, y, w, h);
        this.circ = null;
        this.grid = null;
    }

    private Collider(Mask mask, float x, float y, float r) {
        super(type);
        this.shape = Shape.circ;
        this.mask = mask;
        this.rect = null;
        this.circ = new Circle(x, y, r);
        this.grid = null;
    }

    private Collider(Mask mask, int tileSize, int cols, int rows) {
        super(type);
        this.shape = Shape.grid;
        this.mask = mask;
        this.rect = null;
        this.circ = null;
        this.grid = new Grid(tileSize, cols, rows);
    }

    // Implementation ---------------------------------------------------------

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
        Array<Collider> colliders = World.components.getAll(Collider.type);
        for (var collider : colliders) {
            if (!collider.active) continue;
            if (collider == this) continue;
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

        Position aPosition = a.entity.get(Position.type);
        Position bPosition = b.entity.get(Position.type);
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
        var rect = r.rect;
        var circ = c.circ;
        // TODO...
        return false;
    }

    public boolean overlapsRectGrid(Collider r, Collider g, int xOffset, int yOffset) {
        var rect = r.rect;
        var grid = g.circ;
        // TODO...
        return false;
    }

    public boolean overlapsCircCirc(Collider a, Collider b, int xOffset, int yOffset) {
        // a.circ;
        // b.circ;
        // TODO...
        return false;
    }

    public boolean overlapsCircGrid(Collider c, Collider g, int xOffset, int yOffset) {
        var circ = c.circ;
        var grid = g.circ;
        // TODO...
        return false;
    }
}
