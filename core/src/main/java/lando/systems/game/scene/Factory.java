package lando.systems.game.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.Anims;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.*;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.World;

public class Factory {

    private static Assets assets;

    public static void init(Assets assets) {
        Factory.assets = assets;
    }

    public static Entity heart(float x, float y) {
        var entity = World.entities.create();
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

        var collider = Collider.makeRect(Collider.Mask.effect, -width / 2f, -height / 2f, width, height);

        var mover = new Mover();
        mover.collider = collider;
        mover.speed.setToRandomDirection().scl(MathUtils.random(300, 500));
        mover.setOnHit((params) -> {
            // change the image/tint to indicate a hit
            image.region = heartBroken;
            image.tint.set(tintBroken);

            // change the image back to normal after a bit and self-destruct the timer
            var hitDuration = 0.2f;
            var timer = entity.get(Timer.class);
            if (timer == null) {
                // no active timer, create and attach one
                entity.attach(new Timer(hitDuration, () -> {
                    image.region = heartFull;
                    image.tint.set(tintFull);
                    entity.destroy(Timer.class);
                }), Timer.class);
            } else {
                // timer was still in progress, reset it
                timer.start(hitDuration);
            }

            var hitEntity = params.hitCollider().entity;
            var hitPatch = hitEntity.getIfActive(Patch.class);

            // invert speed on the hit axis
            switch (params.direction()) {
                case LEFT, RIGHT: {
                    mover.invertX();
                    image.scale.set(0.66f, 1.33f);
                    if (hitPatch != null) {
                        hitPatch.scale.set(1.33f, 1f);
                    }
                }
                break;
                case UP, DOWN: {
                    mover.invertY();
                    image.scale.set(1.33f, 0.66f);
                    if (hitPatch != null) {
                        hitPatch.scale.set(1f, 1.33f);
                    }
                }
                break;
            }
        });

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(image, Image.class);
        entity.attach(mover, Mover.class);
        entity.attach(collider, Collider.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }

    public static Entity hero(float x, float y) {
        var entity = World.entities.create();
        var position = new Position(x, y);

        float scale = 4f;
        var animator = new Animator(Anims.Type.HERO_RUN);
        animator.origin.set(8 * scale, 0);
        animator.size.scl(scale);

        var collider = Collider.makeRect(Collider.Mask.npc, -4 * scale, 0, 6 * scale, 12 * scale);

        var mover = new Mover();
        mover.collider = collider;
        mover.gravity = -500f;
        mover.friction = 0.9f;
        mover.speed.set(200, 0);
        mover.setOnHit((params) -> {
            switch (params.direction()) {
                case LEFT, RIGHT: {
                    mover.invertX();
                    animator.scale.scl(0.66f, 1.33f);
                    animator.facing *= -1;
                }
                break;
            }
        });

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(animator, Animator.class);
        entity.attach(mover, Mover.class);
        entity.attach(collider, Collider.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }

    public static Entity boundary(float x, float y, float w, float h) {
        var entity = World.entities.create();

        var halfWidth = w / 2f;
        var halfHeight = h / 2f;
        var position = new Position(x + halfWidth, y + halfHeight);
        var collider = Collider.makeRect(Collider.Mask.solid, -halfWidth, -halfHeight, w, h);
        var patch = new Patch(assets, Patches.Type.PLAIN);
        patch.origin.set(halfWidth, halfHeight);
        patch.size.set(w, h);

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(collider, Collider.class);
        entity.attach(patch, Patch.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }
}
