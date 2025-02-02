package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.scene.components.Mover;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.World;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.screens.BaseScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * An arrangement of {@link Entity} instances from an associated {@link World},
 * setup for them to be created and interact in a particular way to produce
 * a given gameplay or narrative form.
 */
public class Scene<ScreenType extends BaseScreen> {

    public final ScreenType screen;
    public final World world;

    public Scene(ScreenType screen) {
        this.screen = screen;
        this.world = screen.world;

        var margin = 50f;
        var thickness = 20f;
        var camera = screen.worldCamera;
        var width = camera.viewportWidth;
        var height = camera.viewportHeight;
        var centerX = width / 2;
        var centerY = height / 2;

        Factory.heart(centerX, centerY);
        Factory.hero(centerX, height * (2f / 3f));

        // NOTE(brian): this is a clunky way to setup an enclosed region
        //  of colliders, but it works well enough for testing purposes
        Factory.boundary(margin, margin, thickness, height - 2 * margin);
        Factory.boundary(width - margin - thickness, margin, thickness, height - 2 * margin);
        Factory.boundary(margin + thickness, margin, width - 2 * margin - 2 * thickness, thickness);
        Factory.boundary(margin + thickness, height - margin - thickness, width - 2 * margin - 2 * thickness, thickness);

        var interior = new Rectangle(
            margin + thickness,
            margin + thickness,
            width - 2 * (margin + thickness),
            height - 2 * (margin + thickness)
        );

        var tmxFilePath = "maps/home.tmx";
        var solidLayerName = "solid";
        Factory.map(interior.x, interior.y, tmxFilePath, solidLayerName, camera, screen.batch);

        enum CollisionTests { SIMPLE, TILE_GRID, MANY }
        var collisionTest = CollisionTests.TILE_GRID;
        switch (collisionTest) {
            case SIMPLE: {
                // test pairs of circles bouncing off each other and the boundary walls
                var l = Factory.circle(centerX - 200f, centerY, 10f);
                var r = Factory.circle(centerX + 200f, centerY, 10f);
                l.get(Mover.class).speed.set( 300f, 0f);
                r.get(Mover.class).speed.set(-300f, 0f);

                var d = Factory.circle(centerX, centerY - 100f, 10f);
                var u = Factory.circle(centerX, centerY + 100f, 10f);
                d.get(Mover.class).speed.set(0f,  300f);
                u.get(Mover.class).speed.set(0f, -300f);
            } break;
            case TILE_GRID: {
                // test colliding with a grid-shaped collider from a tilemap component
                var vert = Factory.circle(centerX - 420f, centerY + 100f, 10f);
                var horz = Factory.circle(centerX + 200f, centerY - 260f, 10f);
                var diag = Factory.circle(centerX, centerY, 5f);
                vert.get(Mover.class).speed.set(0, -300f);
                horz.get(Mover.class).speed.set(-300f, 0f);
                diag.get(Mover.class).speed.set(-270f, -200f);
            } break;
            case MANY: {
                // test a bunch of circles all randomized and colliding with each other
                var numCircles = 50;
                for (int i = 0; i < numCircles; i++) {
                    var radius = MathUtils.random(5f, 20f);
                    var x = MathUtils.random(interior.x + radius, interior.x + interior.width - radius);
                    var y = MathUtils.random(interior.y + radius, interior.y + interior.height - radius);
                    Factory.circle(x, y, radius);
                }
            } break;
        }
    }

    public void update(float dt) {
        world.update(dt);
    }

    public void render(SpriteBatch batch) {
        world.getFamily(RenderableComponent.class)
            .forEach(component -> {
                if (component.active) {
                    component.render(batch);
                }
            });
    }

    public void render(ShapeDrawer shapes) {
        world.getFamily(RenderableComponent.class)
            .forEach(component -> {
                if (component.active) {
                    component.render(shapes);
                }
            });
    }
}
