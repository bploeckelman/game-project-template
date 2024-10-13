package lando.systems.game.scene.components;

import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callback;

public class Bouncer extends Component {

    private static final String TAG = Bouncer.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Bouncer.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public Collider.Mask collidesWith;
    public Callback<Bouncer.OnHitParams> onHit;

    public static class OnHitParams implements Callback.Params {

        public Collider collidedWith;

        public OnHitParams(Object... params) {
            parse(params);
        }

        public void parse(Object... params) {
            var implName = OnHitParams.class.getName();
            var isValid = validate(1, implName, params);
            if (isValid) {
                if (params[0] instanceof Collider collider) {
                    this.collidedWith = collider;
                } else {
                    isValid = false;
                }
            }

            if (!isValid) {
                throw new GdxRuntimeException("Invalid %s callback params".formatted(implName));
            }
        }
    }

    public Bouncer(Collider.Mask collidesWith) {
        super(type);
        this.collidesWith = collidesWith;
    }

    @Override
    public void update(float dt) {
        if (onHit == null) return;

        Collider collider = entity.get(Collider.type);
        if (collider == null) return;

        var hitCollider = collider.checkAndGet(collidesWith);
        if (hitCollider != null) {
            onHit.run(hitCollider.entity);
        }
    }
}
