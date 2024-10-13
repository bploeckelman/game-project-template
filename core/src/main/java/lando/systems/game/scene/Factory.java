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
        image.bounds.setPosition(position.value);

        var bouncer = new Bouncer(Collider.Mask.solid);
        bouncer.onHit = (params) -> {
            var onHitParams = new Bouncer.OnHitParams(params);
            var collidedWith = onHitParams.collidedWith;

            // TODO(brian): figure out which direction to reflect relative to 'collidedWith'
            //  for now just invert
            mover.speed.scl(-1f);

            // change the image, and change it back to normal after a bit
            image.region = heartBroken;
            entity.attach(new Timer(0.3f, (onEnd) -> image.region = heartFull), Timer.type);
        };

        entity.attach(position, Position.type);
        entity.attach(mover, Mover.type);
        entity.attach(image, Image.type);
        entity.attach(bouncer, Bouncer.type);

        return entity;
    }

    public static Entity boundary(float x, float y, float w, float h) {
        var entity = entities.create();

        var position = new Position(x + w / 2, y + h / 2);
        var collider = Collider.makeRect(Collider.Mask.solid, x, y, w, h);
        var patch = new Patch(assets, Patches.Type.ROUNDED);

        entity.attach(position, Position.type);
        entity.attach(collider, Collider.type);
        entity.attach(patch, Patch.type);

        return entity;
    }
}
