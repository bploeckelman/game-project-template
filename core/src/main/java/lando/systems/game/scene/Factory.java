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
import lando.systems.game.utils.Util;

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

        var collider = Collider.makeRect(Collider.Mask.solid, -width / 2f, -height / 2f, width, height);

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

        var debug = new DebugRender();
        debug.onShapeRender = (params) -> {
            var shapes = params.shapes;

            // draw collider
            var rect = Util.rect.obtain().set(
                collider.rect.x + position.x(),
                collider.rect.y + position.y(),
                collider.rect.width, collider.rect.height);
            shapes.rectangle(rect, Color.MAGENTA, 1f);
            Util.free(rect);

            // draw position
            var outer = 4f;
            var inner = outer * (3f / 4f);
            shapes.filledCircle(position.value, outer, Color.CYAN);
            shapes.filledCircle(position.value, inner, Color.YELLOW);
        };

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

        // TODO(brian): restore the w/h params to set a default size for an Animator
        //  independent of scale, using scale behaves a little counterintuitively
        //  when positioning the sprite relative to the entity's position, especially with animator.facing
        //  I suspect it might be partly due to the way Util.draw() handles the origin,
        //  since it uses the Animator bounds rect created by RenderableComponent which takes the Animator origin into account
        //  the actual Util.draw() override being called doesn't explicitly include the origin values, it just has the rect bounds,
        //  Util.draw(all-args) has to default to something for the origin, so it uses the center of the rect bounds
        //  rather than whatever the RenderableCoomponent's actual origin is....
        //  it was done that way though, because the point of the Util.draw() family is to be a simple one-liner...
        //  *** it might be worth testing:
        //  - use batch.draw(tex, x, y, ox, oy, w, h, sx, sy, rot) / shapes.rectangle(x, y, w, h, ox, oy, rot) directly in RenderableComponent types
        var animator = new Animator(Anims.Type.HERO_IDLE);
        animator.origin.set(8, 0);

        var collider = Collider.makeRect(Collider.Mask.npc, -4, 0, 6, 12);

        var mover = new Mover();
        mover.collider = collider;
        mover.gravity = -100f;
        mover.friction = 0.9f;
        mover.speed.set(100, 0);
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

        var debug = new DebugRender();
        debug.onShapeRender = (params) -> {
            var shapes = params.shapes;

            // draw collider
            var rect = Util.rect.obtain().set(
                collider.rect.x + position.x(),
                collider.rect.y + position.y(),
                collider.rect.width, collider.rect.height);
            shapes.rectangle(rect, Color.MAGENTA, 1f);
            Util.free(rect);

            // draw position
            var outer = 4f;
            var inner = outer * (3f / 4f);
            shapes.filledCircle(position.value, outer, Color.CYAN);
            shapes.filledCircle(position.value, inner, Color.YELLOW);
        };

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

        var debug = new DebugRender();
        debug.onShapeRender = DebugRender.DRAW_POSITION;

        entity.attach(position, Position.class);
        entity.attach(collider, Collider.class);
        entity.attach(patch, Patch.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }
}
