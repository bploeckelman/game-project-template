package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import lando.systems.game.scene.components.Image;
import lando.systems.game.scene.components.Patch;
import lando.systems.game.scene.components.interfaces.RenderableComponent;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entities;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.screens.BaseScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Scene<ScreenType extends BaseScreen> {

    public final ScreenType screen;
    public final Entities entities;

    public final Entity heart;

    public final Entity left;
    public final Entity right;
    public final Entity bottom;
    public final Entity top;

    // -_-
    private final Array<RenderableComponent> renderables = new Array<>();

    public Scene(ScreenType screen) {
        this.screen = screen;
        this.entities = screen.entities;

        var camera = screen.worldCamera;

        var margin = 50f;
        var thickness = 20f;
        var width = camera.viewportWidth;
        var height = camera.viewportHeight;
        var centerX = width / 2;
        var centerY = height / 2;

        // TODO(brian): make position/origin/bounds usage uniform across components
        heart  = Factory.heart(centerX, centerY);
        left   = Factory.boundary(margin, margin, thickness, height - 2 * margin);
        right  = Factory.boundary(width - margin - thickness, margin, thickness, height - 2 * margin);
        bottom = Factory.boundary(margin + thickness, margin, width - 2 * margin - 2 * thickness, thickness);
        top    = Factory.boundary(margin + thickness, height - margin - thickness, width - 2 * margin - 2 * thickness, thickness);
    }

    public void update(float dt) {
        entities.update(dt);

        Array<Image> images = entities.getComponents(Image.type);
        Array<Patch> patches = entities.getComponents(Patch.type);
        renderables.clear();
        renderables.addAll(images);
        renderables.addAll(patches);
    }

    public void render(SpriteBatch batch) {
        renderables.forEach(it -> {
            var component = (Component) it;
            if (component.active) {
                it.render(batch);
            }
        });
    }

    public void render(ShapeDrawer shapes) {
        renderables.forEach(it -> {
            var component = (Component) it;
            if (component.active) {
                it.render(shapes);
            }
        });
    }
}
