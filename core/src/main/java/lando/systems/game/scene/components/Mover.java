package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;

public class Mover extends Component {

    private static final String TAG = Mover.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Mover.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    private final Vector2 remainder = new Vector2();

    public Position position;
    public Collider collider;
    public Vector2 speed;
    public float gravity;
    public float friction;

    public Mover() {
        super(type);
        this.position = null;
        this.collider = null;
        this.speed = new Vector2();
        this.gravity = 0f;
        this.friction = 0f;
    }

    public void update(float dt) {
        // need a position to be moved
        position = entity.get(Position.type);
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

    public boolean moveX(int amount) {
        if (position == null) return false;
        if (collider == null) {
            position.value.x += amount;
        } else {
            // for each pixel, if moving there wouldn't collide then move, otherwise stop
            var sign = Calc.sign(amount);

            while (amount != 0) {
                var isSolid = collider.check(Collider.Mask.solid, sign, 0);
                if (isSolid) {
                    stopX();
                    return true;
                }

                amount -= sign;
                position.value.x += sign;
            }
        }
        return false;
    }

    public boolean moveY(int amount) {
        if (position == null) return false;
        if (collider == null) {
            position.value.y += amount;
        } else {
            // for each pixel, if moving there wouldn't collide then move, otherwise stop
            var sign = Calc.sign(amount);

            while (amount != 0) {
                var isSolid = collider.check(Collider.Mask.solid, 0, sign);
                if (isSolid) {
                    stopY();
                    return true;
                }

                amount -= sign;
                position.value.y += sign;
            }
        }
        return false;
    }
}
