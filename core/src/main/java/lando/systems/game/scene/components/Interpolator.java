package lando.systems.game.scene.components;

import com.badlogic.gdx.math.Interpolation;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.utils.Util;

import java.time.Duration;

/**
 * Simple interpolation in a component for any {@link Entity}
 * that can make use of a single interpolated parameter to control value changes over time.
 * If an {@link Entity} has more complicated interpolation needs;
 * multiple values with different speeds, durations, and easing functions, then this is not the right choice.
 */
public class Interpolator extends Component {

    private static final String TAG = Interpolator.class.getSimpleName();

    private final float duration;

    private float elapsed;
    private float value;
    private boolean paused;

    public Interpolation interpolation;

    /**
     * Multiplier for elapsed time accumulator:
     * <strong>must be > 0, if <= 0 reset to 1</strong>
     */
    public float speed;

    public Interpolator(Entity entity, Duration duration) {
        this(entity, duration.toSeconds());
    }

    public Interpolator(Entity entity, float duration) {
        this(entity, duration, Interpolation.linear);
    }

    public Interpolator(Entity entity, float duration, Interpolation interpolation) {
        super(entity);
        this.duration = duration;
        this.elapsed = 0;
        this.value = 0;
        this.paused = false;
        this.interpolation = interpolation;
        this.speed = 1;
    }

    /**
     * Interpolate between {@code start} and {@code end} values
     * based on current interpolated percentage [0..1] from this component.
     */
    public float apply(float start, float end) {
        return start + (end - start) * value;
    }

    @Override
    public void update(float deltaTime) {
        if (inactive()) return;
        if (paused) return;

        // enforce speed multiplier constraint
        if (speed <= 0) {
            Util.log(TAG, "Entity(%d) has invalid speed multiplier %.2f <= 0 - reset to 1".formatted(entity.id, speed));
            speed = 1f;
        }

        // elapse some time
        elapsed += speed * deltaTime;

        // constrain elapsed time to a percentage, ie. in [0..1]
        var alpha = Calc.clampf(elapsed / duration, 0f, 1f);

        // apply the interpolation function to the percentage completed to get an interpolated percent [0..1]
        value = interpolation.apply(alpha);
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void reset() {
        elapsed = 0;
        value = 0;
    }

    public float duration() { return duration; }

    public float elapsed() { return elapsed; }

    public float value() { return value; }

    public float inverseValue() { return 1f - value; }

    public boolean isPaused() { return paused; }

    public boolean isRunning() { return !paused; }
}
