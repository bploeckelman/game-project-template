package lando.systems.game.scene.components;

import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callbacks;

public class Timer extends Component {

    private float duration;

    public Callbacks.NoArg onEnd;

    public Timer() {
        this.duration = 0;
        this.onEnd = null;
    }

    public Timer(float duration) {
        this(duration, null);
    }

    public Timer(float duration, Callbacks.NoArg onEnd) {
        this.onEnd = onEnd;
        start(duration);
    }

    public void start(float duration) {
        this.duration = duration;
    }

    @Override
    public void update(float dt) {
        if (duration > 0) {
            duration -= dt;
            if (duration <= 0 && onEnd != null) {
                onEnd.run();
            }
        }
    }
}
