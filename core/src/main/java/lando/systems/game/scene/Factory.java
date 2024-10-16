package lando.systems.game.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.Collider;
import lando.systems.game.scene.components.Image;
import lando.systems.game.scene.components.Mover;
import lando.systems.game.scene.components.Patch;
import lando.systems.game.scene.components.Position;
import lando.systems.game.scene.components.Timer;
import lando.systems.game.scene.framework.Entities;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.utils.Callback;

public class Factory {

    private static Assets assets;
    private static Entities entities;

    public static void init(Assets assets, Entities entities) {
        Factory.assets = assets;
        Factory.entities = entities;
    }

    public static Entity heart(float x, float y) {
        var entity = entities.create();

        var position = new Position(x, y);

        var heartFull = assets.get(Icons.class, Icons.Type.HEART);
        var heartBroken = assets.get(Icons.class, Icons.Type.HEART_BROKEN);
        var tintFull = Color.RED.cpy();
        var tintBroken = Color.ORANGE.cpy();
        var width = heartFull.getRegionWidth();
        var height = heartFull.getRegionHeight();
        var image = new Image(heartFull);
        image.tint.set(tintFull);
        image.origin.set(width / 2f, height / 2f);

        var collider = Collider.makeRect(Collider.Mask.solid, -width / 2f, -height / 2f, width, height);

        var mover = new Mover();

        var speed = MathUtils.random(500, 800);
        mover.speed.setToRandomDirection().scl(speed);

        var onHit = (Callback<Mover.OnHitParams>) (params) -> {
            // change the image/tint to indicate a hit
            image.region = heartBroken;
            image.tint.set(tintBroken);

            // change the image back to normal after a bit and self-destruct the timer
            var hitDuration = 0.2f;
            var timer = (Timer) entity.get(Timer.type);
            if (timer == null) {
                // no active timer, create and attach one
                entity.attach(new Timer(hitDuration, (onEnd) -> {
                    image.region = heartFull;
                    image.tint.set(tintFull);
                    entity.destroy(Timer.type);
                }), Timer.type);
            } else {
                // timer was still in progress, reset it
                timer.start(hitDuration);
            }

            // invert speed on the hit axis
            // NOTE: the simpler `mover.speed.[axis] *= -1f` would work,
            //   except we need to make sure to clear `mover.remainder.[axis]`
            //   so no extra remainder pushes us through into the hit collider
            switch (params.direction()) {
                case LEFT, RIGHT: {
                    float newSpeedX = -mover.speed.x;
                    mover.stopX();
                    mover.speed.x = newSpeedX;
                    image.scale.set(0.66f, 1.33f);
                } break;
                case UP, DOWN: {
                    float newSpeedY = -mover.speed.y;
                    mover.stopY();
                    mover.speed.y = newSpeedY;
                    image.scale.set(1.33f, 0.66f);
                } break;
            }
        };
        mover.onHitX = onHit;
        mover.onHitY = onHit;

        entity.attach(position, Position.type);
        entity.attach(mover, Mover.type);
        entity.attach(image, Image.type);
        entity.attach(collider, Collider.type);

        return entity;
    }

    public static Entity boundary(float x, float y, float w, float h) {
        var entity = entities.create();

        var position = new Position(x, y);
        var collider = Collider.makeRect(Collider.Mask.solid, 0, 0, w, h);
        var patch = new Patch(assets, Patches.Type.PLAIN);
        patch.size.set(w, h);

        entity.attach(position, Position.type);
        entity.attach(collider, Collider.type);
        entity.attach(patch, Patch.type);

        return entity;
    }
}
