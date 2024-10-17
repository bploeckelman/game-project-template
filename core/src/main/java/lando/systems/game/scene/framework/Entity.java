package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.game.utils.Util;

public class Entity {

    private static final String TAG = Entity.class.getSimpleName();

    // Fixed values indicating 'no entity' for components that are not attached to any specific entity
    public static final int NONE_ID = 0;
    public static final Entity NONE = new Entity(NONE_ID);

    /**
     * Internal counter used for assigning globally unique {@link Entity#id} values to new entities
     */
    private static int NEXT_ID = 1;

    /**
     * Each {@link Entity} holds references to its attached {@link Component} instances
     * for ease of lookup from other attached components to enable interaction.
     * These should be considered 'weak' references, as the primary container
     * for all components is in {@link World}. The {@code int key} for this map
     * and for the global map in {@code World.componentsMap} is the concrete
     * {@link Component} sub-class {@code MyComponent.type} field.
     * See the comment in {@link Component} for details on how to create new component types.
     */
    final IntMap<Component> componentMap = new IntMap<>();

    /**
     * Globally unique id for this {@link Entity} instance
     */
    public final int id;

    /**
     * Flag indicating whether this {@link Entity} instance should be ignored or not.
     */
    public boolean active;

    /**
     * Constructor has package-private visibility
     * limiting creation to {@link World#create()}
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

    /**
     * Get the component of the specified type which is attached to this entity if one exists.
     * NOTE: when calling this method, the variable used to store the return value
     *   must be declared explicitly using the concrete {@link Component} sub-class type,
     *   using {@code var} for local variable type inference will infer the most general type,
     *   in this case {@link Component} rather than {@link C}
     * @param componentTypeId the type of the component to get (eg. {@code MyComponent.type})
     * @param <C> generic type of the component to retrieve
     * @return the attached component of type {@link C} if one exists, null otherwise
     */
    @SuppressWarnings("unchecked")
    public <C extends Component> C get(int componentTypeId) {
        return (C) componentMap.get(componentTypeId);
    }

    // TODO(brian): make sure detach/destroy are handled correctly throughout

    /**
     * Attach the specified component to this entity in the slot reserved for
     * the specified component type.
     * NOTE:
     *   - Component constructors automatically add the new component to the global Entities.componentsMap
     *   - This method assumes there's not a component of the specified type attached to this entity already,
     *     if there might be, use {@link #replace} instead, which behaves the same as this method
     *     if no component of that type is already attached.
     */
    public void attach(Component component, int componentTypeId) {
        var existing = componentMap.get(componentTypeId);
        if (existing != null) {
            component.entity = NONE;
            var clazz = Component.TYPES.get(componentTypeId);
            Util.log(TAG,
                "Unable to add %s(%d) to entity %d, only one component per type allowed, use replace(component) instead "
                .formatted(clazz.getSimpleName(), componentTypeId, id));
            return;
        }
        component.entity = this;
        componentMap.put(componentTypeId, component);
    }

    /**
     * Detach the component with the specified type from this entity and return it,
     * if a component of the specified type is attached, do nothing and return null otherwise.
     * NOTE: this *does not* destroy the component, so use {@link #destroy} instead if appropriate.
     */
    public Component detach(int componentTypeId) {
        var component = componentMap.remove(componentTypeId);
        if (component != null) {
            component.entity = NONE;
        }
        return component;
    }

    /**
     * Detach and destroy any existing component of the specified type,
     * and attach the specified component to this entity in its place
     */
    public void replace(Component component, int componentTypeId) {
        destroy(componentTypeId);
        componentMap.put(componentTypeId, component);
    }

    /**
     * Detach and destroy any existing component of the specified type if one exists,
     * do nothing otherwise.
     */
    public void destroy(int componentTypeId) {
        var existing = detach(componentTypeId);
        if (existing != null) {
            existing.entity = NONE;
            World.components.destroy(existing, componentTypeId);
        }
    }

    /**
     * Detach and destroy all components attached to this entity if any,
     * do nothing otherwise.
     */
    public void clear() {
        Util.log(TAG, "Removing all components from entity %d!".formatted(id));
        var keys = componentMap.keys().toArray();
        for (int i = keys.size - 1; i >= 0; i--) {
            var componentTypeId = keys.get(i);
            var component = detach(componentTypeId);
            World.components.destroy(component, componentTypeId);
        }
        componentMap.clear();
    }
}
