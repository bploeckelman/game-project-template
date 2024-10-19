package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public final Entity heart;
    public final Entity hero;

    public final Entity left;
    public final Entity right;
    public final Entity bottom;
    public final Entity top;

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

        heart = Factory.heart(centerX, centerY);
        hero = Factory.hero(centerX, height / 4f);
        left = Factory.boundary(margin, margin, thickness, height - 2 * margin);
        right = Factory.boundary(width - margin - thickness, margin, thickness, height - 2 * margin);
        bottom = Factory.boundary(margin + thickness, margin, width - 2 * margin - 2 * thickness, thickness);
        top = Factory.boundary(margin + thickness, height - margin - thickness, width - 2 * margin - 2 * thickness, thickness);
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
