package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callbacks;
import lando.systems.game.utils.Direction;

public class Mover extends Component {

    private final Vector2 remainder = new Vector2();

    public Collider collider;
    public Callbacks.TypedArg<OnHitParams> onHitX;
    public Callbacks.TypedArg<OnHitParams> onHitY;
    public Vector2 speed;
    public float gravity;
    public float friction;

    public record OnHitParams(Collider hitCollider, Direction.Relative direction)
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

    public void update(float dt) {
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

    public boolean onGround() {
        if (collider == null) return false;

        var hitSolid = collider.check(Collider.Mask.solid, 0, -1);

        return hitSolid;
    }

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
                var hitCollider = collider.checkAndGet(Collider.Mask.solid, sign, 0);
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
                Collider hitCollider = collider.checkAndGet(Collider.Mask.solid, 0, sign);
                if (hitCollider != null) {
                    if (onHitY != null) {
                        var onHitParams = new OnHitParams(collider, Direction.Relative.from(sign, Direction.Axis.Y));
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
