package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.game.Main;
import lando.systems.game.utils.Util;

public class Entity {

    private static final String TAG = Entity.class.getSimpleName();

    private static int NEXT_ID = 1;

    // TODO(brian): add methods to differentiate between 'remove/destroy' component
    // Fixed values indicating 'no entity' for Components that are detached but not destroyed
    public static final int NONE_ID = 0;
    public static final Entity NONE = new Entity(NONE_ID);

    /**
     * Each {@link Entity} holds references to its attached {@link Component} instances
     * for ease of lookup from other attached components to enable interaction.
     * These should be considered 'weak' references, as the primary container
     * for all components is in {@link Entities}.
     * The map key is {@link Component#mask()} which is unique per {@link Component} mask.
     */
    final IntMap<Component> componentMap = new IntMap<>();

    public final int id;

    public boolean active;

    /**
     * Constructor has package-private visibility
     * limiting creation to {@link Entities#create()}
     */
    Entity() {
        this.id = NEXT_ID++;
        this.active = true;
    }

    /**
     * Private constructor for instantiating the {@link Entity#NONE} instance
     */
    private Entity(int noneId) {
        if (NONE_ID != noneId) {
            throw new GdxRuntimeException(TAG + ": Usage error, Entity(int) is only for instantiating Entity.NONE");
        }

        this.id = noneId;
        this.active = false;
    }

    @SuppressWarnings("unchecked")
    public <C extends Component> C get(int componentTypeId) {
        return (C) componentMap.get(componentTypeId);
    }

    // TODO(brian): make sure detach/destroy are handled correctly throughout

    public void attach(Component component, int componentTypeId) {
        // NOTE: Component constructor handles adding instance to Entities.componentsMap
        var existing = componentMap.get(componentTypeId);
        if (existing != null) {
            component.entity = NONE;
            var clazz = Component.TYPES.get(componentTypeId);
            Util.log(TAG, """
                    Unable to add %s(%d) to entity %d,
                    only one component per mask allowed,
                    use replace(component) instead
                    """.formatted(clazz.getSimpleName(), componentTypeId, id));
            return;
        }
        component.entity = this;
        componentMap.put(componentTypeId, component);
    }

    public Component detach(int componentTypeId) {
        var component = componentMap.remove(componentTypeId);
        if (component != null) {
            component.entity = NONE;
        }
        return component;
    }

    public void replace(Component component, int componentTypeId) {
        remove(componentTypeId);
        // TODO(brian): what does this do to an existing component, destroy or detach? (probably destroy)
        componentMap.put(componentTypeId, component);
    }

    public void remove(int componentTypeId) {
        var existing = detach(componentTypeId);
        if (existing != null) {
            existing.entity = NONE;
            Main.game.entities.removeComponent(existing, componentTypeId);
        }
    }

    public void clear() {
        Util.log(TAG, "Removing all components from entity %d!".formatted(id));
        for (var entry : componentMap.entries()) {
            var clazz = entry.key;
            var component = entry.value;
            component.entity = NONE;
            Main.game.entities.removeComponent(component, clazz);
        }
        componentMap.clear();
    }
}
