package lando.systems.game.scene;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import lando.systems.game.Main;
import lando.systems.game.utils.Util;

public abstract class Component {

    private static final String TAG = Component.class.getSimpleName();

    public final Entity entity;

    public boolean active;

    // TODO(brian): invert the control here, no direct construction, acquire through Entities.createComponent(Class<T>);
    public <ComponentType extends Component> Component(Entity entity, Class<ComponentType> clazz) {
        this.entity = entity;
        this.active = true;

        if (!ClassReflection.isAssignableFrom(clazz, getClass())) {
            Util.log(TAG, "Reflection error, %s is not assignable from %s".formatted(clazz, getClass()));
        }
        var component = clazz.cast(this);
        Main.game.entities.add(component, clazz);
    }

    public void update(float dt) {}

    @Override
    public String toString() {
        return "%s(entity: %d)".formatted(getClass().getSimpleName(), entity.id);
    }
}
