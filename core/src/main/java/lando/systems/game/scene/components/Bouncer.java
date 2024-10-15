package lando.systems.game.scene.components;

import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callback;
import lando.systems.game.utils.Direction;

public class Bouncer extends Component {

    private static final String TAG = Bouncer.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Bouncer.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public Collider.Mask collidesWith;
    public Callback<OnHitParams> onHit;
    public Collider collider;
    public Mover mover;

    public static class OnHitParams implements Callback.Params {

        public Collider xHitCollider;
        public Collider yHitCollider;
        public Direction.Relative xDir;
        public Direction.Relative yDir;

        public OnHitParams(Collider xHitCollider, Collider yHitCollider, Direction.Relative xDir, Direction.Relative yDir) {
            this.xHitCollider = xHitCollider;
            this.yHitCollider = yHitCollider;
            this.xDir = xDir;
            this.yDir = yDir;
        }
    }

    public Bouncer(Collider.Mask collidesWith) {
        super(type);
        this.collidesWith = collidesWith;
        this.onHit = null;
        this.collider = null;
    }

    @Override
    public void update(float dt) {
        if (onHit == null) return;

        collider = entity.get(Collider.type);
        if (collider == null) return;

        mover = entity.get(Mover.type);
        if (mover == null) return;

        var xSign = (int) Calc.sign(mover.speed.x);
        var ySign = (int) Calc.sign(mover.speed.y);
        var xHitCollider = collider.checkAndGet(collidesWith, xSign, 0);
        var yHitCollider = collider.checkAndGet(collidesWith, 0, ySign);

        Direction.Relative xDir = null;
        Direction.Relative yDir = null;
        if (xHitCollider != null) {
            if      (xSign < 0) xDir = Direction.Relative.LEFT;
            else if (xSign > 0) xDir = Direction.Relative.RIGHT;
        }
        if (yHitCollider != null) {
            if      (ySign < 0) yDir = Direction.Relative.DOWN;
            else if (ySign > 0) yDir = Direction.Relative.UP;
        }

        if (xHitCollider != null || yHitCollider != null) {
            onHit.run(new OnHitParams(xHitCollider, yHitCollider, xDir, yDir));
        }
    }
}
