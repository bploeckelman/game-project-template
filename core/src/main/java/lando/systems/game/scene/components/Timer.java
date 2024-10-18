package lando.systems.game.scene.components;

import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Callbacks;

public class Timer extends Component {

    private static final String TAG = Timer.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Timer.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    private float duration;

    public Callbacks.NoArg onEnd;

    public Timer() {
        super(type);
    }

    public Timer(float duration) {
        super(type);
        start(duration);
    }

    public Timer(float duration, Callbacks.NoArg onEnd) {
        super(type);
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
