package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callbacks;
import lando.systems.game.utils.Direction;

import java.util.EnumSet;
import java.util.List;

public class Mover extends Component {

    private final Vector2 remainder = new Vector2();

    /**
     * Specifies {@link Collider.Mask} types should be
     * checked for possible collisions. Defaults to {@link Collider.Mask#solid},
     * but can be modified by the {@code *CollidesWith(Collider.Mask... masks)}
     * methods.
     */
    private final EnumSet<Collider.Mask> collidesWith = EnumSet.of(Collider.Mask.solid);

    public Collider2 collider;
    public Callbacks.TypedArg<OnHitParams> onHitX;
    public Callbacks.TypedArg<OnHitParams> onHitY;
    public Vector2 speed;
    public float gravity;
    public float friction;

    public record OnHitParams(Collider2 hitCollider, Direction.Relative direction)
        implements Callbacks.TypedArg.Params {
    }

    public Mover() {
        this.collider = null;
        this.onHitX = null;
        this.onHitY = null;
        this.speed = new Vector2();
        this.gravity = 0f;
        this.friction = 0f;
    }

    // ------------------------------------------------------------------------
    // Methods for modifying which types of colliders are checked
    // ------------------------------------------------------------------------

    public void setCollidesWith(Collider.Mask... masks) {
        collidesWith.clear();
        addCollidesWith(masks);
    }

    public void addCollidesWith(Collider.Mask... masks) {
        collidesWith.addAll(List.of(masks));
    }

    public void removeCollidesWith(Collider.Mask... masks) {
        List.of(masks).forEach(collidesWith::remove);
    }

    // ------------------------------------------------------------------------
    // Methods to set the OnHit callbacks after construction
    // ------------------------------------------------------------------------

    public void setOnHit(Callbacks.TypedArg<OnHitParams> onHit) {
        setOnHit(onHit, onHit);
    }

    public void setOnHit(Callbacks.TypedArg<OnHitParams> onHitX, Callbacks.TypedArg<OnHitParams> onHitY) {
        this.onHitX = onHitX;
        this.onHitY = onHitY;
    }

    // ------------------------------------------------------------------------
    // Update on each frame, when active
    // ------------------------------------------------------------------------

    @Override
    public void update(float dt) {
        if (!active) return;

        // need a position to be moved
        var position = entity.getIfActive(Position.class);
        if (position == null) return;

        // apply friction, maybe
        if (friction > 0 && onGround()) {
            speed.x = Calc.approach(speed.x, 0, friction * dt);
        }

        // apply gravity, maybe
        if (gravity != 0 && !onGround()) {
            speed.y += gravity * dt;
        }

        // how far should we move this tick, assuming nothing is in the way
        float xTotal = remainder.x + speed.x * dt;
        float yTotal = remainder.y + speed.y * dt;

        // round to integer values because we only move a pixel at a time
        int xAmount = (int) xTotal;
        int yAmount = (int) yTotal;

        // track the fractional remainder so we don't lose any movement between ticks
        remainder.x = xTotal - xAmount;
        remainder.y = yTotal - yAmount;

        // apply the movement
        moveX(xAmount);
        moveY(yAmount);
    }

    /**
     * Quick test for whether this mover can be considered to be 'on the ground',
     * which means there is a solid collider directly underneath it.
     * NOTE: mostly for typical 2d platformer based games and may not be appropriate for other game types.
     */
    public boolean onGround() {
        if (collider == null) return false;

        // NOTE(brian): this is a bit of a workaround to make sure gravity is always applied
        //  for objects which don't interact with the tilemap / solid colliders like characters
        if (!collidesWith.contains(Collider.Mask.solid)) {
            return false;
        }

        // is there a solid collider 1 pixel directly below this mover's associated collider?
        var hitSolid = collider.check(Collider.Mask.solid, 0, -1);

        return hitSolid;
    }

    // ------------------------------------------------------------------------
    // Methods to modify this mover's speed in various ways
    // ------------------------------------------------------------------------

    public void stopX() {
        speed.x = 0f;
        remainder.x = 0f;
    }

    public void stopY() {
        speed.y = 0f;
        remainder.y = 0f;
    }

    public void invertX() {
        speed.x *= -1f;
        remainder.x = 0f;
    }

    public void invertY() {
        speed.y *= -1f;
        remainder.y = 0f;
    }

    // ------------------------------------------------------------------------
    // Methods to trigger a movement by a certain number of pixels on an axis
    // ------------------------------------------------------------------------

    /**
     * Attempt to move on the x (horizontal) axis by the specified amount (in pixels).
     * NOTE: mostly for internal use in {@link #update(float dt)}, which will attempt
     *  to move on both axes based on the current speed, but they can also be used
     *  for 'out of band' movement, especially if there is no {@link Collider} associated
     *  with the {@link lando.systems.game.scene.framework.Entity} that this {@link Mover}
     *  component is attached to.
     */
    public boolean moveX(int amount) {
        var position = entity.getIfActive(Position.class);
        if (position == null) return false;

        if (collider == null) {
            position.value.x += amount;
            return true;
        } else {
            // for each pixel, if moving there wouldn't collide then move,
            // otherwise run onHit callback or stop if no callback is set
            var sign = Calc.sign(amount);

            while (amount != 0) {
                var hitCollider = collider.checkAndGet(collidesWith, sign, 0);
                if (hitCollider != null) {
                    if (onHitX != null) {
                        var onHitParams = new OnHitParams(hitCollider, Direction.Relative.from(sign, Direction.Axis.X));
                        onHitX.run(onHitParams);
                    } else {
                        stopX();
                    }
                    return true;
                }

                amount -= sign;
                position.value.x += sign;
            }
        }
        return false;
    }

    /**
     * Attempt to move on the y (vertical) axis by the specified amount (in pixels).
     * NOTE: mostly for internal use in {@link #update(float dt)}, which will attempt
     *  to move on both axes based on the current speed, but they can also be used
     *  for 'out of band' movement, especially if there is no {@link Collider} associated
     *  with the {@link lando.systems.game.scene.framework.Entity} that this {@link Mover}
     *  component is attached to.
     */
    public boolean moveY(int amount) {
        var position = entity.getIfActive(Position.class);
        if (position == null) return false;

        if (collider == null) {
            position.value.y += amount;
            return true;
        } else {
            // for each pixel, if moving there wouldn't collide then move,
            // otherwise run onHit callback or stop if no callback is set
            var sign = Calc.sign(amount);

            while (amount != 0) {
                var hitCollider = collider.checkAndGet(collidesWith, 0, sign);
                if (hitCollider != null) {
                    if (onHitY != null) {
                        var onHitParams = new OnHitParams(hitCollider, Direction.Relative.from(sign, Direction.Axis.Y));
                        onHitY.run(onHitParams);
                    } else {
                        stopY();
                    }
                    return true;
                }

                amount -= sign;
                position.value.y += sign;
            }
        }
        return false;
    }
}
