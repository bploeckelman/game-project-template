package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.game.utils.Util;

import java.util.Optional;

/**
 * Container for {@link Entity} and {@link Component} instances.
 * Operations on entities and components can be globally accessed via the
 * facade implementations: {@link World#entities} and {@link World#components}.
 */
public class World implements EntityFacade, ComponentFacade {

    private static final String TAG = World.class.getSimpleName();

    public static EntityFacade entities;
    public static ComponentFacade components;

    private final IntMap<Entity> entitiesMap = new IntMap<>();
    private final IntMap<Array<? extends Component>> componentsMap = new IntMap<>();

    public World() {
        World.entities = this;
        World.components = this;
    }

    /**
     * Updates all active components
     */
    public void update(float dt) {
        for (var components : componentsMap.values()) {
            for (var component : components) {
                if (!component.active) continue;
                component.update(dt);
            }
        }
    }

    // ------------------------------------------------------------------------
    // EntityFacade implementation
    // ------------------------------------------------------------------------

    /**
     * Lookup an entity by id
     *
     * @param id the integer id of the requested entity
     * @return optional containing the entity instance if found, empty optional otherwise
     */
    @Override
    public Optional<Entity> get(int id) {
        var entity = entitiesMap.get(id);
        if (entity == null) {
            Util.log(TAG, "Entity %d not found".formatted(id));
        }
        return Optional.ofNullable(entity);
    }

    /**
     * Instantiate a new {@link Entity}
     * TODO(brian): add pooling
     */
    @Override
    public Entity create() {
        var entity = new Entity();
        entitiesMap.put(entity.id, entity);
        return entity;
    }

    /**
     * Destroys the specified {@link Entity} along with any attached {@link Component} instances
     * TODO(brian): add pooling
     */
    @Override
    public void destroy(Entity entity) {
        if (entity == null) {
            Util.log(TAG, "destroy() called with null Entity value");
            return;
        }

        // all entity instances should be tracked here, double check and warn if not found
        if (!entitiesMap.containsKey(entity.id)) {
            Util.log(TAG, "Entity %d not found, may indicate dangling references".formatted(entity.id));
        }

        // remove any components of any type associated with this entity
        for (int i = 0; i < Component.TYPE_IDS.size; i++) {
            int componentTypeId = Component.TYPE_IDS.get(i);
            entity.destroy(componentTypeId);
        }

        // remove the entity itself allowing it to be garbage collected
        entitiesMap.remove(entity.id);
    }

    /**
     * Remove <strong>all</strong> {@link Entity} instances and their attached {@link Component} instances
     */
    public void clear() {
        Util.log(TAG, "Destroying all entities and their attached components!");
        for (int i = entitiesMap.size - 1; i >= 0; i--) {
            var entity = entitiesMap.get(i);
            destroy(entity);
        }
        entitiesMap.clear();
    }

    // ------------------------------------------------------------------------
    // ComponentFacade implementation
    // ------------------------------------------------------------------------

    /**
     * Get all components of the specified type
     *
     * @param componentTypeId the unique id of the {@link Component} type of the components to retrieve
     * @return non-null array containing all components of the given type, if any
     */
    @SuppressWarnings("unchecked")
    @Override
    public <C extends Component> Array<C> getAll(int componentTypeId) {
        Array<C> components;
        if (componentsMap.containsKey(componentTypeId)) {
            components = (Array<C>) componentsMap.get(componentTypeId);
        } else {
            components = new Array<>();
            componentsMap.put(componentTypeId, components);
        }
        return components;
    }

    /**
     * Add a new component to the global collection, keyed by type
     *
     * @param component       the {@link Component} to add
     * @param componentTypeId the unique id of the {@link Component} type of the component to add
     */
    @Override
    public void add(Component component, int componentTypeId) {
        if (component == null) {
            Util.log(TAG, "add() called with null Component value");
            return;
        }

        var components = getAll(componentTypeId);
        components.add(component);
    }

    /**
     * Remove the specified component from the global collection, if they're in it
     *
     * @param component       the {@link Component} to remove
     * @param componentTypeId the unique id of the component's concrete type
     */
    @Override
    public void destroy(Component component, int componentTypeId) {
        if (component == null) {
            Util.log(TAG, "removeComponent(): component null, ignoring");
            return;
        }

        var entity = component.entity;
        if (entity != Entity.NONE) {
            Util.log(TAG, "removeComponent(): component attached to entity %d, detaching".formatted(entity.id));
            entity.detach(componentTypeId);
        }

        var components = getAll(componentTypeId);
        components.removeValue(component, true);
    }
}
