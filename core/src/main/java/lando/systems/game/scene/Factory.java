package lando.systems.game.scene;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.Bouncer;
import lando.systems.game.scene.components.Collider;
import lando.systems.game.scene.components.Image;
import lando.systems.game.scene.components.Mover;
import lando.systems.game.scene.components.Patch;
import lando.systems.game.scene.components.Position;
import lando.systems.game.scene.components.Timer;
import lando.systems.game.scene.framework.Entities;
import lando.systems.game.scene.framework.Entity;

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
        var mover = new Mover();
        var speed = MathUtils.random(100, 200);
        mover.speed.setToRandomDirection().scl(speed);

        var heartFull = assets.get(Icons.class, Icons.Type.HEART);
        var heartBroken = assets.get(Icons.class, Icons.Type.HEART_BROKEN);
        var image = new Image(heartFull);

        var width = heartFull.getRegionWidth();
        var height = heartFull.getRegionHeight();
        var collider = Collider.makeRect(Collider.Mask.solid, -width / 2f, -height / 2f, width, height);

        var bouncer = new Bouncer(Collider.Mask.solid);
        bouncer.onHit = (params) -> {
            // change the image to indicate a hit
            image.region = heartBroken;

            // change the image back to normal after a bit and self-destruct the timer
            entity.attach(new Timer(0.3f, (onEnd) -> {
                image.region = heartFull;
                entity.destroy(Timer.type);
            }), Timer.type);

            // invert the mover's speed on whichever axis/axes hit a collider
            if (params.xDir != null) mover.speed.x *= -1f;
            if (params.yDir != null) mover.speed.y *= -1f;
        };

        entity.attach(position, Position.type);
        entity.attach(mover, Mover.type);
        entity.attach(image, Image.type);
        entity.attach(collider, Collider.type);
        entity.attach(bouncer, Bouncer.type);

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
