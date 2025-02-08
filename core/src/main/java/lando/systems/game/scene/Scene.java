package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.game.Config;
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
    public final World<ScreenType> world;

    public Scene(ScreenType screen) {
        this.screen = screen;
        this.world = new World<>(this);

        // reset the screen's world camera to default for each new scene
        var camera = screen.worldCamera;
        camera.setToOrtho(false, Config.framebuffer_width, Config.framebuffer_height);
        camera.update();
    }

    public Entity createEntity() {
        return world.create(this);
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
