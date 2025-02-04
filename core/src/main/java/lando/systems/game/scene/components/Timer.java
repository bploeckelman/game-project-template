package lando.systems.game.scene.components;

import lando.systems.game.scene.framework.Component;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.utils.Callbacks;

public class Timer extends Component {

    private float duration;

    public Callbacks.NoArg onEnd;

    public Timer(Entity entity) {
        super(entity);
        this.duration = 0;
        this.onEnd = null;
    }

    public Timer(Entity entity, float duration) {
        this(entity, duration, null);
    }

    public Timer(Entity entity, float duration, Callbacks.NoArg onEnd) {
        super(entity);
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
