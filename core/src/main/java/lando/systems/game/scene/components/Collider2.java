package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.ComponentFamily;
import lando.systems.game.scene.framework.World;
import lando.systems.game.utils.Util;

import java.util.EnumSet;

import static lando.systems.game.scene.components.Collider.Mask;

public class Collider2 extends ComponentFamily {

    private static final String TAG = Collider2.class.getSimpleName();

    public final Mask mask;
    public final Shape shape;

//    public enum Mask { solid, npc, player, projectile, effect }

    public sealed interface Shape permits RectShape, CircShape, GridShape {
        boolean overlaps(Collider2 other, int xOffset, int yOffset);
    }

    public static Collider2 makeRect(Mask mask, float x, float y, float w, float h) {
        if (w <= 0 || h <= 0) {
            Util.log(TAG, "WARN: collider created with degenerate shape size");
        }
        return new Collider2(mask, x, y, w, h);
    }

    public static Collider2 makeCirc(Mask mask, float x, float y, float radius) {
        if (radius <= 0) {
            Util.log(TAG, "WARN: collider created with degenerate shape size");
        }
        return new Collider2(mask, x, y, radius);
    }

    public static Collider2 makeGrid(Mask mask, int tileSize, int cols, int rows) {
        if (tileSize <= 0 || cols <= 0 || rows <= 0) {
            Util.log(TAG, "WARN: collider created with degenerate shape size");
        }
        return new Collider2(mask, tileSize, cols, rows);
    }

    private Collider2(Mask mask, float x, float y, float w, float h) {
        this.mask = mask;
        this.shape = new RectShape(this, x, y, w, h);
    }

    private Collider2(Mask mask, float x, float y, float radius) {
        this.mask = mask;
        this.shape = new CircShape(this, x, y, radius);
    }

    private Collider2(Mask mask, int tileSize, int cols, int rows) {
        this.mask = mask;
        this.shape = new GridShape(this, tileSize, cols, rows);
    }

    public <T extends Shape> T shape(Class<T> shapeClass) {
        if (ClassReflection.isInstance(shapeClass, shape)) {
            return shapeClass.cast(shape);
        }
        throw new GdxRuntimeException("Collider shape is not the specified type: " + shapeClass);
    }

    public boolean check(Mask mask) {
        return check(mask, 0, 0);
    }

    public boolean check(Mask mask, int xOffset, int yOffset) {
        var hitCollider = checkAndGet(mask, xOffset, yOffset);
        return hitCollider != null;
    }

    public Collider2 checkAndGet(Mask mask, int xOffset, int yOffset) {
        var colliders = World.components.getComponents(Collider2.class);
        for (var other : colliders) {
            if (other == this) continue;
            if (other.notActive()) continue;
            if (mask != other.mask) continue;

            if (shape.overlaps(other, xOffset, yOffset)) {
                return other;
            }
        }
        return null;
    }

    public Collider2 checkAndGet(EnumSet<Mask> masks,  int xOffset, int yOffset) {
        var colliders = World.components.getComponents(Collider2.class);
        for (var other : colliders) {
            if (other == this) continue;
            if (other.notActive()) continue;
            if (!masks.contains(other.mask)) continue;

            if (shape.overlaps(other, xOffset, yOffset)) {
                return other;
            }
        }
        return null;
    }

    public static final class RectShape implements Shape {

        private final Collider2 collider;

        public final Rectangle rect;

        public RectShape(Collider2 collider, float x, float y, float w, float h) {
            this.collider = collider;
            this.rect = new Rectangle(x, y, w, h);
        }

        @Override
        public boolean overlaps(Collider2 other, int xOffset, int yOffset) {
            var aRect = Util.rect.obtain().set(0, 0, 0, 0);
            var aPos = Util.vec2.obtain().setZero();
            var bPos = Util.vec2.obtain().setZero();

            var aPosition = collider.entity.get(Position.class);
            var bPosition = other.entity.get(Position.class);
            if (aPosition != null && aPosition.active) aPos.set(aPosition.value);
            if (bPosition != null && bPosition.active) bPos.set(bPosition.value);

            aRect.set(
                rect.x + aPos.x + xOffset,
                rect.y + aPos.y + yOffset,
                rect.width, rect.height);

            var overlaps = false;
            if (other.shape instanceof RectShape otherShape) {
                var bRect = Util.rect.obtain();

                bRect.set(
                    otherShape.rect.x + bPos.x,
                    otherShape.rect.y + bPos.y,
                    otherShape.rect.width,
                    otherShape.rect.height
                );
                overlaps = aRect.overlaps(bRect);

                Util.free(bRect);
            } else if (other.shape instanceof CircShape otherShape) {
                var bCirc = Util.circ.obtain();

                bCirc.set(
                    otherShape.circ.x + bPos.x,
                    otherShape.circ.y + bPos.y,
                    otherShape.circ.radius
                );
                overlaps = Intersector.overlaps(bCirc, aRect);

                Util.free(bCirc);
            } else if (other.shape instanceof GridShape otherGrid) {
                var gridBounds = Util.rect.obtain().set(0, 0, 0, 0);

                var rows = otherGrid.rows;
                var cols = otherGrid.cols;
                var tileSize = otherGrid.tileSize;

                // adjust this shape's rectangle to be positioned relative to the grid
                aRect.setPosition(
                    rect.x + aPos.x + xOffset - bPos.x,
                    rect.y + aPos.y + yOffset - bPos.y
                );

                // construct the rectangle describing the boundary of the grid
                gridBounds.set(bPos.x, bPos.y, cols * tileSize, rows * tileSize);

                // only worth checking against the grid tiles if the rectangle is within the grid bounds
                if (aRect.overlaps(gridBounds)) {
                    // get the range of grid tiles that the rectangle overlaps on each axis
                    int left   = Calc.clampInt((int) Calc.floor(aRect.x                    / (float) tileSize), 0, cols);
                    int right  = Calc.clampInt((int) Calc.ceiling((aRect.x + aRect.width)  / (float) tileSize), 0, cols);
                    int top    = Calc.clampInt((int) Calc.ceiling((aRect.y + aRect.height) / (float) tileSize), 0, rows);
                    int bottom = Calc.clampInt((int) Calc.floor(aRect.y                    / (float) tileSize), 0, rows);

                    // check each tile in the possible overlap range for solidity
                    for (int y = bottom; y < top; y++) {
                        for (int x = left; x < right; x++) {
                            var i = x + y * cols;
                            var solid = otherGrid.tiles[i].solid;
                            if (solid) {
                                overlaps = true;
                                break;
                            }
                        }
                    }
                }

                Util.free(gridBounds);
            }

            Util.free(bPos);
            Util.free(aPos);
            Util.free(aRect);
            return overlaps;
        }
    }

    public static final class CircShape implements Shape {

        private final Collider2 collider;

        public final Circle circ;

        public CircShape(Collider2 collider, float x, float y, float radius) {
            this.collider = collider;
            this.circ = new Circle(x, y, radius);
        }

        @Override
        public boolean overlaps(Collider2 other, int xOffset, int yOffset) {
            var aCirc = Util.circ.obtain();
            var aPos = Util.vec2.obtain().setZero();
            var bPos = Util.vec2.obtain().setZero();

            var aPosition = collider.entity.get(Position.class);
            var bPosition = other.entity.get(Position.class);
            if (aPosition != null && aPosition.active) aPos.set(aPosition.value);
            if (bPosition != null && bPosition.active) bPos.set(bPosition.value);

            aCirc.set(
                circ.x + aPos.x + xOffset,
                circ.y + aPos.y + yOffset,
                circ.radius
            );

            var overlaps = false;
            if (other.shape instanceof RectShape otherShape) {
                var bRect = Util.rect.obtain();

                bRect.set(
                    otherShape.rect.x + bPos.x,
                    otherShape.rect.y + bPos.y,
                    otherShape.rect.width,
                    otherShape.rect.height
                );
                overlaps = Intersector.overlaps(aCirc, bRect);

                Util.free(bRect);
            } else if (other.shape instanceof CircShape otherShape) {
                var bCirc = Util.circ.obtain();

                bCirc.set(
                    otherShape.circ.x + bPos.x,
                    otherShape.circ.y + bPos.y,
                    otherShape.circ.radius
                );
                overlaps = aCirc.overlaps(bCirc);

                Util.circ.free(bCirc);
            } else if (other.shape instanceof GridShape otherGrid) {
                var gridBounds = Util.rect.obtain().set(0, 0, 0, 0);

                var rows = otherGrid.rows;
                var cols = otherGrid.cols;
                var tileSize = otherGrid.tileSize;

                // construct the rectangle describing the boundary of the grid
                gridBounds.set(bPos.x, bPos.y, cols * tileSize, rows * tileSize);

                // only worth checking against the grid tiles if the circle is within the grid bounds
                if (Intersector.overlaps(aCirc, gridBounds)) {
                    // calc the rectangular extents of the circle relative to the grid (instead of relative to the world)
                    // this is needed so that we can determine what horiz/vert ranges of tiles could have an overlap
                    var circRelativeX = circ.x + aPos.x + xOffset - bPos.x;
                    var circRelativeY = circ.y + aPos.y + yOffset - bPos.y;
                    var circLeft   = circRelativeX - circ.radius;
                    var circRight  = circRelativeX + circ.radius;
                    var circTop    = circRelativeY + circ.radius;
                    var circBottom = circRelativeY - circ.radius;

                    // get the range of grid tiles that the circle overlaps on each axis
                    int left   = Calc.clampInt((int) Calc.floor(  circLeft   / (float) tileSize), 0, cols);
                    int right  = Calc.clampInt((int) Calc.ceiling(circRight  / (float) tileSize), 0, cols);
                    int top    = Calc.clampInt((int) Calc.ceiling(circTop    / (float) tileSize), 0, rows);
                    int bottom = Calc.clampInt((int) Calc.floor(  circBottom / (float) tileSize), 0, rows);

                    // check each tile in the possible overlap range for solidity
                    var tileRect = Util.rect.obtain();
                    for (int y = bottom; y < top; y++) {
                        for (int x = left; x < right; x++) {
                            var i = x + y * cols;
                            var solid = otherGrid.tiles[i].solid;
                            if (solid) {
                                tileRect.set(
                                    gridBounds.x + x * tileSize,
                                    gridBounds.y + y * tileSize,
                                    tileSize, tileSize
                                );

                                overlaps = Intersector.overlaps(aCirc, tileRect);
                                if (overlaps) {
                                    break;
                                }
                            }
                        }
                    }
                    Util.free(tileRect);
                }

                Util.free(gridBounds);
            }

            Util.free(bPos);
            Util.free(aPos);
            Util.free(aCirc);
            return overlaps;
        }
    }

    public static final class GridShape implements Shape {

        private final Collider2 collider;

        public final int tileSize;
        public final int cols;
        public final int rows;
        public final Tile[] tiles;

        public static class Tile  {
            public boolean solid;
        }

        public GridShape(Collider2 collider, int tileSize, int cols, int rows) {
            this.collider = collider;
            this.tileSize = tileSize;
            this.cols = cols;
            this.rows = rows;
            this.tiles = new Tile[cols*rows];
            for (int i = 0; i < cols*rows; i++) {
                tiles[i] = new Tile();
            }
        }

        public void set(int x, int y, boolean solid) {
            var inRangeX = Calc.between(x, 0, cols - 1);
            var inRangeY = Calc.between(y, 0, rows - 1);
            if (!inRangeX || !inRangeY) {
                Util.log(TAG, "Collider.grid.set(%d, %d, %b) called with out of bounds coords, ignored"
                    .formatted(x, y, solid));
                return;
            }
            int index = x + y * cols;
            tiles[index].solid = solid;
        }

        @Override
        public boolean overlaps(Collider2 other, int xOffset, int yOffset) {
            throw new UnsupportedOperationException("grid->* overlap checks are not supported, such checks should go in the other direction");
        }
    }
}
