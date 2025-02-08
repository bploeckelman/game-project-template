package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.utils.Util;

import java.util.Objects;

public class ViewController extends Component {

    public final Vector2 speed = new Vector2(500f, 2000f);

    public final Boundary boundary;

    public Target target;

    private boolean initialized;

    public ViewController(Entity entity, Boundary boundary) {
        this(entity, boundary, null);
    }

    public ViewController(Entity entity, Boundary boundary, Target target) {
        super(entity);
        this.boundary = Objects.requireNonNull(boundary);
        this.target = target;
        this.initialized = false;
    }

    public void target(float x, float y) {
        target(new Vector2(x, y));
    }

    public void target(Position position) {
        var newTarget = new PositionTarget(position);
        target(newTarget);
    }

    public void target(Vector2 vector2) {
        var newTarget = new Vec2Target(vector2);
        target(newTarget);
    }

    public void target(Interpolator interpolator) {
        var newTarget = new ScrollTarget(interpolator);
        target(newTarget);
    }

    public void target(Target newTarget) {
        target = newTarget;
        initialized = false;
    }

    @Override
    public void update(float dt) {
        if (inactive()) return;
        if (target == null) return;

        // get orthographic camera from parent entity's viewer component
        var viewer = entity.get(Viewer.class);
        if (viewer == null) {
            Util.log("ViewController entity missing expected Viewer component");
            return;
        }
        var camera = viewer.camera;

        // set initial values for target position
        if (!initialized) {
            initialized = true;
            camera.position.set(target.x(), target.y(), 0);
            camera.update();
        }

        // zoom to fit the boundary width
        // TODO(brian): need a way to override this for manual zooming
        camera.zoom = boundary.bounds.width / camera.viewportWidth;

        // get half dimensions of the camera viewport, adjusted for the zoom factor
        var camHalfWidth  = viewer.width() / 2f;
        var camHalfHeight = viewer.height() / 2f;

        // follow target
        var x = Calc.approach(camera.position.x, target.x(), dt * speed.x);
        var y = Calc.approach(camera.position.y, target.y(), dt * speed.y);

        // contain within boundary
        var bounds = boundary.bounds;
        var left   = bounds.x + camHalfWidth;
        var bottom = bounds.y + camHalfHeight;
        var right  = bounds.x + bounds.width  - camHalfWidth;
        var top    = bounds.y + bounds.height - camHalfHeight;
        x = Calc.clampf(x, left, right);
        y = Calc.clampf(y, bottom, top);

        // update actual camera position
        camera.position.set(x, y, 0);
    }

    public sealed interface Target permits PositionTarget, Vec2Target, ScrollTarget {
        float x();
        float y();
    }

    private record PositionTarget(Position position) implements Target {
        public float x() { return position.x(); }
        public float y() { return position.y(); }
    }

    private record Vec2Target(Vector2 value) implements Target {
        public float x() { return value.x; }
        public float y() { return value.y; }
    }

    private final class ScrollTarget implements Target {
        Interpolator interpolator;

        public ScrollTarget(Interpolator interpolator) {
            this.interpolator = interpolator;
        }

        public float x() { return 0; }

        public float y() {
            var viewCtrl = ViewController.this;
            var boundary = viewCtrl.boundary;
            var viewer = viewCtrl.entity.get(Viewer.class);
            if (viewer == null) {
                Util.log("ViewController entity missing expected Viewer component");
                return boundary.bottom();
            }

            // calculate current target y pos for camera, adjusting interp min/max y for viewer's centered origin
            var viewerOffset = viewer.height() / 2f;
            var min = boundary.bottom() + viewerOffset;
            var max = boundary.top()    + viewerOffset;
            return interpolator.apply(min, max);
        }
    }
}
