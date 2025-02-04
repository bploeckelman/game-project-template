package lando.systems.game.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.Anims;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.*;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.screens.BaseScreen;
import lando.systems.game.utils.Time;
import lando.systems.game.utils.Util;

public class EntityFactory {

    public static Entity heart(Scene<? extends BaseScreen> scene, float x, float y) {
        var entity = scene.createEntity();

        var assets = scene.screen.assets;

        new Position(entity, x, y);

        var heartFull = assets.get(Icons.class, Icons.Type.HEART);
        var heartBroken = assets.get(Icons.class, Icons.Type.HEART_BROKEN);
        var tintFull = Color.RED.cpy();
        var tintBroken = Color.ORANGE.cpy();
        var width = heartFull.getRegionWidth();
        var height = heartFull.getRegionHeight();

        var image = new Image(entity, heartFull);
        image.tint.set(tintFull);
        image.origin.set(width / 2f, height / 2f);

        var collider = Collider.makeRect(entity, Collider.Mask.effect, -width / 2f, -height / 2f, width, height);

        var mover = new Mover(entity, collider);
        mover.speed.setToRandomDirection().scl(MathUtils.random(300, 500));
        mover.addCollidesWith(Collider.Mask.npc);
        mover.setOnHit((params) -> {
            // change the image/tint to indicate a hit
            image.set(heartBroken);
            image.tint.set(tintBroken);

            // change the image back to normal after a bit and self-destruct the timer
            var hitDuration = 0.2f;
            var timer = entity.get(Timer.class);
            if (timer == null) {
                // no active timer, create and attach one
                new Timer(entity, hitDuration, () -> {
                    image.set(heartFull);
                    image.tint.set(tintFull);
                    entity.destroy(Timer.class);
                });
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

        DebugRender.makeForShapes(entity, DebugRender.DRAW_POSITION_AND_COLLIDER);

        return entity;
    }

    public static Entity circle(Scene<? extends BaseScreen> scene, float x, float y, float radius) {
        var entity = scene.createEntity();

        new Position(entity, x, y);

        var assets = scene.screen.assets;
        var region = assets.atlas.findRegion("objects/circle");
        var image = new Image(entity, region);
        image.size.set(2 * radius, 2 * radius);
        image.origin.set(radius, radius);
        image.tint.set(Util.randomColorPastel());

        var collider = Collider.makeCirc(entity, Collider.Mask.object, 0, 0, radius);

        var speed = MathUtils.random(100f, 300f);
        var mover = new Mover(entity, collider);
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

        DebugRender.makeForShapes(entity, DebugRender.DRAW_POSITION_AND_COLLIDER);

        return entity;
    }

    public static Entity hero(Scene<? extends BaseScreen> scene, float x, float y) {
        var entity = scene.createEntity();

        new Position(entity, x, y);

        float scale = 4f;
        var animator = new Animator(entity, Anims.Type.HERO_FALL);
        animator.origin.set(8 * scale, 0);
        animator.size.scl(scale);

        var collider = Collider.makeRect(entity, Collider.Mask.npc, -4 * scale, 0, 6 * scale, 12 * scale);

        var mover = new Mover(entity, collider);
        mover.gravity = -500f;
        mover.speed.set(350, 0);
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
                    // NOTE(brian): example use of Timer component for rudimentary game logic, 'self-destructing' when complete
                    var duration = 0.3f;
                    var timer = entity.get(Timer.class);
                    if (timer != null) {
                        // timer was still in progress, reset it
                        timer.start(duration);
                    } else {
                        new Timer(entity, duration, () -> {
                            // turn around
                            animator.facing *= -1;
                            // resume moving in the opposite direction
                            mover.speed.x = speedX;
                            // jump!
                            mover.speed.y = 500;

                            // self-destruct the timer
                            entity.destroy(Timer.class);
                        });
                    }
                }
                break;
            }
        });

        // behavior 'component' - example of an anonymous component used to implement simple game logic
        new Component(entity) {
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

        DebugRender.makeForShapes(entity, DebugRender.DRAW_POSITION_AND_COLLIDER);

        return entity;
    }

    public static Entity boundary(Scene<? extends BaseScreen> scene, float x, float y, float w, float h) {
        var entity = scene.createEntity();

        var halfWidth = w / 2f;
        var halfHeight = h / 2f;

        new Position(entity, x + halfWidth, y + halfHeight);
        Collider.makeRect(entity, Collider.Mask.solid, -halfWidth, -halfHeight, w, h);

        var patch = new Patch(entity, Patches.Type.PLAIN);
        patch.origin.set(halfWidth, halfHeight);
        patch.size.set(w, h);

        DebugRender.makeForShapes(entity, DebugRender.DRAW_POSITION_AND_COLLIDER);

        return entity;
    }

    public static Entity map(Scene<? extends BaseScreen> scene, float x, float y, String tmxFilePath, String solidLayerName) {
        var entity = scene.createEntity();

        new Position(entity, x, y);

        var tilemap = new Tilemap(entity, tmxFilePath, scene.screen.worldCamera,  scene.screen.batch);
        tilemap.makeGridCollider(solidLayerName);
        tilemap.makeBoundary();

        DebugRender.makeForShapes(entity, DebugRender.DRAW_POSITION_AND_COLLIDER);

        return entity;
    }
}
