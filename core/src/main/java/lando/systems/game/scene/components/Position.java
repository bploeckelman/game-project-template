package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.scene.framework.Component;

// TODO(brian): use tettinger's gdcrux type here instead so we don't need to reimplement a bunch of the conversions
public class Position extends Component {

    private static final String TAG = Position.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Position.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public final Vector2 value = new Vector2();

    public Position(float x, float y) {
        super(type);
        this.value.set(x, y);
    }

    public Position(Vector2 value) {
        this(value.x, value.y);
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
