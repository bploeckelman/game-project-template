package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;

public class Boundary extends Component {

    // TODO(brian): support other shapes through a shared interface if needed
    public final Rectangle bounds = new Rectangle();

    public Boundary(Entity entity, Rectangle bounds) {
        super(entity);
        this.bounds.set(bounds);
    }

    public Boundary(Entity entity, float x, float y, float width, float height) {
        super(entity);
        this.bounds.set(x, y, width, height);
    }

    public float left()   { return bounds.x; }
    public float bottom() { return bounds.y; }
    public float right()  { return bounds.x + bounds.width; }
    public float top()    { return bounds.y + bounds.height; }

    public float halfWidth() { return bounds.width / 2; }
    public float halfHeight() { return bounds.height / 2; }
}
