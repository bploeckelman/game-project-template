package lando.systems.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.scene.Scene;
import lando.systems.game.scene.components.Mover;
import lando.systems.game.screens.GameScreen;

public class SceneTest extends Scene<GameScreen> {

    public SceneTest(GameScreen screen) {
        super(screen);

        var margin = 50f;
        var thickness = 20f;
        var camera = screen.worldCamera;
        var width = camera.viewportWidth;
        var height = camera.viewportHeight;
        var centerX = width / 2;
        var centerY = height / 2;

        EntityFactory.heart(this, centerX, centerY);
        EntityFactory.hero(this, centerX, height * (2f / 3f));

        // NOTE(brian): this is a clunky way to setup an enclosed region
        //  of colliders, but it works well enough for testing purposes
        EntityFactory.boundary(this, margin, margin, thickness, height - 2 * margin);
        EntityFactory.boundary(this, width - margin - thickness, margin, thickness, height - 2 * margin);
        EntityFactory.boundary(this, margin + thickness, margin, width - 2 * margin - 2 * thickness, thickness);
        EntityFactory.boundary(this, margin + thickness, height - margin - thickness, width - 2 * margin - 2 * thickness, thickness);

        var interior = new Rectangle(
            margin + thickness,
            margin + thickness,
            width - 2 * (margin + thickness),
            height - 2 * (margin + thickness)
        );

        var tmxFilePath = "maps/test/home.tmx";
        var solidLayerName = "solid";
        EntityFactory.map(this, interior.x, interior.y, tmxFilePath, solidLayerName);

        enum CollisionTests { SIMPLE, TILE_GRID, MANY }
        var collisionTest = CollisionTests.TILE_GRID;
        switch (collisionTest) {
            case SIMPLE: {
                // test pairs of circles bouncing off each other and the boundary walls
                var l = EntityFactory.circle(this, centerX - 200f, centerY, 10f);
                var r = EntityFactory.circle(this, centerX + 200f, centerY, 10f);
                l.get(Mover.class).speed.set( 300f, 0f);
                r.get(Mover.class).speed.set(-300f, 0f);

                var d = EntityFactory.circle(this, centerX, centerY - 100f, 10f);
                var u = EntityFactory.circle(this, centerX, centerY + 100f, 10f);
                d.get(Mover.class).speed.set(0f,  300f);
                u.get(Mover.class).speed.set(0f, -300f);
            } break;
            case TILE_GRID: {
                // test colliding with a grid-shaped collider from a tilemap component
                var vert = EntityFactory.circle(this, centerX - 420f, centerY + 100f, 10f);
                var horz = EntityFactory.circle(this, centerX + 200f, centerY - 260f, 10f);
                var diag = EntityFactory.circle(this, centerX, centerY, 5f);
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
                    EntityFactory.circle(this, x, y, radius);
                }
            } break;
        }
    }
}
