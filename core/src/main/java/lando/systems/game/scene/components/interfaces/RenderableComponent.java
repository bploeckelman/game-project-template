package lando.systems.game.scene.components.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

public interface RenderableComponent {
    void render(SpriteBatch batch);
    void render(ShapeDrawer shapes);
}
