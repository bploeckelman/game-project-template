package lando.systems.game.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.Anims;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.*;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.World;
import lando.systems.game.utils.Time;
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

        var collider = Collider2.makeRect(Collider.Mask.effect, -width / 2f, -height / 2f, width, height);

        var mover = new Mover();
        mover.collider = collider;
        mover.addCollidesWith(Collider.Mask.npc);
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
        entity.attach(collider, Collider2.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }

    public static Entity circle(float x, float y, float radius) {
        var entity = World.entities.create();

        var position = new Position(x, y);

        var region = assets.atlas.findRegion("objects/circle");
        var image = new Image(region);
        image.size.set(2 * radius, 2 * radius);
        image.origin.set(radius, radius);
        image.tint.set(Util.randomColorPastel());

        var collider = Collider2.makeCirc(Collider.Mask.object, 0, 0, radius);

        var speed = MathUtils.random(100f, 300f);
        var mover = new Mover();
        mover.collider = collider;
        mover.speed.setToRandomDirection().scl(speed);
        mover.addCollidesWith(Collider.Mask.object, Collider.Mask.solid);
        mover.setOnHit((params) -> {
            // invert speed on the hit axis and add some squash/stretch
            switch (params.direction()) {
                case LEFT, RIGHT: {
                    mover.invertX();
                    image.scale.set(0.66f, 1.33f);
                } break;
                case UP, DOWN: {
                    mover.invertY();
                    image.scale.set(1.33f, 0.66f);
                } break;
            }
        });

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(image, Image.class);
        entity.attach(mover, Mover.class);
        entity.attach(collider, Collider2.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }

    public static Entity hero(float x, float y) {
        var entity = World.entities.create();
        var position = new Position(x, y);

        float scale = 4f;
        var animator = new Animator(Anims.Type.HERO_FALL);
        animator.origin.set(8 * scale, 0);
        animator.size.scl(scale);

        var collider = Collider2.makeRect(Collider.Mask.npc, -4 * scale, 0, 6 * scale, 12 * scale);

        var mover = new Mover();
        mover.collider = collider;
        mover.gravity = -500f;
        mover.speed.set(400, 0);
        mover.setOnHit((params) -> {
            switch (params.direction()) {
                case LEFT, RIGHT: {
                    // invert and save the new speed, then stop for a bit
                    mover.invertX();
                    var speedX = mover.speed.x;
                    mover.stopX();

                    // do an 'oof'
                    Time.pause_for(0.1f);
                    animator.scale.scl(0.66f, 1.33f);

                    // take a moment to recover
                    var duration = 0.3f;
                    var timer = entity.get(Timer.class);
                    if (timer != null) {
                        // timer was still in progress, reset it
                        timer.start(duration);
                    } else {
                        timer = new Timer(duration, () -> {
                            // turn around
                            animator.facing *= -1;
                            // resume moving in the opposite direction
                            mover.speed.x = speedX;
                            // jump!
                            mover.speed.y = 500;

                            // self-destruct the timer
                            entity.destroy(Timer.class);
                        });
                        entity.attach(timer, Timer.class);
                    }
                }
                break;
            }
        });

        var behavior = new Component() {
            @Override
            public void update(float dt) {
                if (mover.onGround()) {
                    if (mover.speed.x != 0) {
                        animator.play(Anims.Type.HERO_RUN);
                    } else {
                        animator.play(Anims.Type.HERO_IDLE);
                    }
                } else {
                    if (mover.speed.y > 0) {
                        animator.play(Anims.Type.HERO_JUMP);
                    } else if (mover.speed.y < 0) {
                        animator.play(Anims.Type.HERO_FALL);
                    }
                }
            }
        };

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(animator, Animator.class);
        entity.attach(mover, Mover.class);
        entity.attach(collider, Collider2.class);
        entity.attach(debug, DebugRender.class);
        entity.attach(behavior, Component.class);

        return entity;
    }

    public static Entity boundary(float x, float y, float w, float h) {
        var entity = World.entities.create();

        var halfWidth = w / 2f;
        var halfHeight = h / 2f;
        var position = new Position(x + halfWidth, y + halfHeight);
        var collider = Collider2.makeRect(Collider.Mask.solid, -halfWidth, -halfHeight, w, h);
        var patch = new Patch(assets, Patches.Type.PLAIN);
        patch.origin.set(halfWidth, halfHeight);
        patch.size.set(w, h);

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(collider, Collider2.class);
        entity.attach(patch, Patch.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }

    public static Entity map(float x, float y, String tmxFilePath, String solidLayerName, OrthographicCamera camera, SpriteBatch batch) {
        var entity = World.entities.create();

        var position = new Position(x, y);

        var tilemap = new Tilemap(tmxFilePath, camera,  batch);
        var collider = tilemap.makeGridCollider(solidLayerName);
        var boundary = tilemap.makeBoundary();

        var debug = DebugRender.makeForShapes(DebugRender.DRAW_POSITION_AND_COLLIDER);

        entity.attach(position, Position.class);
        entity.attach(collider, Collider2.class);
        entity.attach(tilemap, Tilemap.class);
        entity.attach(boundary, Boundary.class);
        entity.attach(debug, DebugRender.class);

        return entity;
    }
}
