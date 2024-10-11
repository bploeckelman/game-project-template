package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.game.scene.Component;
import lando.systems.game.scene.Entity;
import space.earlygrey.shapedrawer.ShapeDrawer;

// TODO(brian): doesn't do much for us so far since we're not able to store/lookup by superclass
public abstract class VisualComponent extends Component {

    public final Color tint = Color.WHITE.cpy();

    public <ComponentType extends Component> VisualComponent(Entity entity, Class<ComponentType> clazz) {
        super(entity, clazz);
    }

    public abstract void render(SpriteBatch batch);

    public void render(ShapeDrawer shapes) {}
}
