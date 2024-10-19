package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.gdcrux.PointF2;
import lando.systems.game.scene.framework.Component;

public class Position extends Component {

    public final PointF2 value;

    public Position() {
        this(0, 0);
    }

    public Position(Vector2 value) {
        this(value.x, value.y);
    }

    public Position(int x, int y) {
        this.value = new PointF2(x, y);
    }

    public Position(float x, float y) {
        this.value = new PointF2(x, y);
    }

    public float x() {
        return value.x;
    }

    public float y() {
        return value.y;
    }

    public int xi() {
        return value.xi();
    }

    public int yi() {
        return value.yi();
    }

    public Position set(float x, float y) {
        value.set(x, y);
        return this;
    }

    public Position set(int x, int y) {
        value.set(x, y);
        return this;
    }

    public Position set(Vector2 value) {
        this.value.set(value);
        return this;
    }

    public Position zero() {
        value.set(0, 0);
        return this;
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
