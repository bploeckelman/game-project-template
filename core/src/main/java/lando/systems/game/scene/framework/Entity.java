package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.game.utils.Util;

import java.util.HashMap;
import java.util.Map;

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
     * for all components is in {@link World}. The {@link Class} instance is a {@code key}
     * for this map and for the global map in {@code World.componentsByClass}.
     */
    final Map<Class<? extends Component>, Component> componentsByClass = new HashMap<>();

    /**
     * Globally unique id for this {@link Entity} instance
     */
    public final int id;

    /**
     * Flag indicating whether this {@link Entity} instance should be ignored or not.
     */
    public boolean active;

    /**
     * Package-private constructor limiting {@link Entity} creation to {@link World#create()}
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
     *
     * @param clazz the {@link Class} of the {@link Component} to get (eg. {@code MyComponent.class})
     * @param <C>   generic type of the component to get
     * @return the attached component of type {@link C} if one exists, null otherwise
     */
    public <C extends Component> C get(Class<C> clazz) {
        return clazz.cast(componentsByClass.get(clazz));
    }

    /**
     * Get the component of the specified type which is attached to this entity if one exists and {@link Component#active}
     *
     * @param clazz the {@link Class} of the {@link Component} to get (eg. {@code MyComponent.class})
     * @param <C>   generic type of the component to get
     * @return the attached component of type {@link C} if one exists and is active, null otherwise
     */
    public <C extends Component> C getIfActive(Class<C> clazz) {
        var component = get(clazz);
        if (component != null && component.active) {
            return component;
        }
        return null;
    }

    /**
     * Attach the specified component to this entity in the slot reserved for
     * the specified component type, unless a component of that type is already attached.
     * The {@link Component} constructor automatically adds each new component to the global
     * {@link World}{@code .componentsByClass} map.
     *
     * @param component the {@link Component} to attach
     * @param clazz     the {@link Class} of the {@link Component} to attach (eg. {@code MyComponent.class})
     * @param <C>       generic type of the component to attach
     */
    public <C extends Component> void attach(C component, Class<C> clazz) {
        var existing = componentsByClass.get(clazz);
        if (existing != null) {
            component.entity = NONE;
            Util.log(TAG, "%s already attached to entity %d, use replace()".formatted(clazz.getSimpleName(), id));
            return;
        }

        component.entity = this;
        componentsByClass.put(clazz, component);
    }

    /**
     * Detach the component with the specified type from this entity and return it,
     * if a component of the specified type is attached, do nothing and return null otherwise.
     * NOTE: this *does not* destroy the component, so use {@link #destroy} instead if appropriate.
     *
     * @param clazz the {@link Class} of the {@link Component} to detach (eg. {@code MyComponent.class})
     * @param <C>   generic type of the component to detach
     */
    public <C extends Component> C detach(Class<C> clazz) {
        var component = componentsByClass.remove(clazz);
        if (component == null) return null;

        component.entity = NONE;
        return clazz.cast(component);
    }

    /**
     * Detach and destroy any existing component of the specified type,
     * and attach the specified component to this entity in its place
     *
     * @param component the {@link Component} to replace
     * @param clazz     the {@link Class} of the {@link Component} to replace (eg. {@code MyComponent.class})
     * @param <C>       generic type of the component to replace
     */
    public <C extends Component> void replace(C component, Class<C> clazz) {
        destroy(clazz);
        componentsByClass.put(clazz, component);
    }

    /**
     * Detach and destroy any existing component of the specified type if one exists,
     * do nothing otherwise.
     *
     * @param clazz the {@link Class} of the {@link Component} to destroy (eg. {@code MyComponent.class})
     * @param <C>   generic type of the component to destroy
     */
    public <C extends Component> void destroy(Class<C> clazz) {
        var component = detach(clazz);
        if (component != null) {
            component.entity = NONE;
            World.components.destroy(component, clazz);
        }
    }

    /**
     * Detach and destroy all components attached to this entity if any,
     * do nothing otherwise.
     */
    public void clear() {
        Util.log(TAG, "Removing all components from entity %d!".formatted(id));
        componentsByClass.forEach((clazz, component) -> {
            detach(clazz);
            World.components.destroy(component, clazz);
        });
        componentsByClass.clear();
    }
}
