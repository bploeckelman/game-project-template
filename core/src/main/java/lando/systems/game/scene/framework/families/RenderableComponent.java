package lando.systems.game.scene.framework.families;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.game.scene.framework.ComponentFamily;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class RenderableComponent extends ComponentFamily {
    public abstract void render(SpriteBatch batch);

    public void render(ShapeDrawer shapes) {
        // default no-op implementation since this is less likely to be used than the SpriteBatch version
    }
}
