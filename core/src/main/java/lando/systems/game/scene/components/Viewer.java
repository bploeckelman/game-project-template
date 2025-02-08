package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.OrthographicCamera;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;

public class Viewer extends Component {

    public final OrthographicCamera camera;

    public Viewer(Entity entity, OrthographicCamera camera) {
        super(entity);
        this.camera = camera;
    }

    @Override
    public void update(float delta) {
        camera.update();
    }

    public float width() {
        return camera.viewportWidth * camera.zoom;
    }

    public float height() {
        return camera.viewportHeight * camera.zoom;
    }

    public float left() {
        return camera.position.x - width() / 2f;
    }

    public float right() {
        return camera.position.x + width() / 2f;
    }

    public float bottom() {
        return camera.position.y - height() / 2f;
    }

    public float top() {
        return camera.position.y + height() / 2f;
    }
}
