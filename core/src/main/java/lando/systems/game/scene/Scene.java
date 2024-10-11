package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.components.Image;
import lando.systems.game.scene.components.Patch;
import lando.systems.game.scene.components.VisualComponent;
import lando.systems.game.scene.entities.TestEntity;
import lando.systems.game.screens.BaseScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Scene<ScreenType extends BaseScreen> {

    public final ScreenType screen;
    public final Entities entities;

    private final Entity player;
    private final Entity background;
    private final Array<VisualComponent> visuals = new Array<>();

    public Scene(ScreenType screen) {
        this.screen = screen;
        this.entities = screen.entities;

        var assets = screen.assets;
        var camera = screen.worldCamera;

        var centerX = (int) camera.viewportWidth / 2;
        var centerY = (int) camera.viewportHeight / 2;
        player = new TestEntity(assets, centerX, centerY);

        background = entities.create();
        var margin = 50f;
        var patch = new Patch(background, assets, Patches.Type.ROUNDED);
        patch.bounds.set(margin, margin,
            camera.viewportWidth - 2 * margin,
            camera.viewportHeight - 2 * margin);
    }

    public void update(float dt) {
        entities.update(dt);

        var images = entities.get(Image.class);
        var patches = entities.get(Patch.class);
        visuals.clear();
        visuals.addAll(images);
        visuals.addAll(patches);
    }

    public void render(SpriteBatch batch) {
        for (var visual : visuals) {
            visual.render(batch);
        }
    }

    public void render(ShapeDrawer shapes) {
        for (var visual : visuals) {
            visual.render(shapes);
        }
    }
}
