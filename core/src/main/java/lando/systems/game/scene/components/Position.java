package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.scene.Component;
import lando.systems.game.scene.Entity;

public class Position extends Component {

    public final Vector2 value = new Vector2();

    public Position(Entity entity, float x, float y) {
        super(entity, Position.class);
        this.value.set(x, y);
    }

    public Position(Entity entity, Vector2 value) {
        this(entity, value.x, value.y);
    }

    public float x() { return value.x; }
    public float y() { return value.y; }
    public int xi() { return (int) value.x; }
    public int yi() { return (int) value.y; }

    public Position set(float x, float y) {
        value.set(x, y);
        return this;
    }

    public Position set(int x, int y) {
        return set(x, y);
    }

    public Position set(Vector2 value) {
        return set(value.x, value.y);
    }

    public Position zero() {
        return set(0, 0);
    }

    public Position move(float x, float y) {
        value.add(x, y);
        return this;
    }

    public Position move(int x, int y) {
        return move(x, y);
    }

    public Position move(Vector2 value) {
        return move(value.x, value.y);
    }
}
