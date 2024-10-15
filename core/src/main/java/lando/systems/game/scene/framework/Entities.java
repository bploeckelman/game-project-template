package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.game.utils.Util;

import java.util.Optional;

// TODO(brian): thinking about a static self ref to enable usage like: Entities.all.get|add|remove() or similar
//  - could go a step further and create separate entity/component facades
//  - static instance for each that could call just the relevant methods, entity vs component
// TODO(brian): what should hold an instance to this? Main, BaseScreen, Scene, all, other?
//  - key?: do entities need to persist between screens/scenes
//  - if scoped to BaseScreen then get via 'Main.currentScreen'
//  - if scoped to Scene then get via Main.currentScreen.scene'
//  - regardless, could be accessed globally via getter in Main like: Main.game.entities()
public class Entities {

    private static final String TAG = Entities.class.getSimpleName();

    private final IntMap<Entity> entities = new IntMap<>();
    private final IntMap<Array<? extends Component>> componentsMap = new IntMap<>();

    /**
     * Updates all components
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
    // Entity operations
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new {@link Entity}
     * TODO(brian): add pooling
     */
    public Entity create() {
        var entity = new Entity();
        entities.put(entity.id, entity);
        return entity;
    }

    /**
     * Destroys the specified {@link Entity} along with any attached {@link Component} instances
     * TODO(brian): add pooling
     */
    public void destroy(Entity entity) {
        if (entity == null) {
            Util.log(TAG, "destroy() called with null Entity value");
            return;
        }

        // all entity instances should be tracked here, double check and warn if not found
        if (!entities.containsKey(entity.id)) {
            Util.log(TAG, "Entity %d not found, may indicate dangling references".formatted(entity.id));
        }

        // remove any components of any type associated with this entity
        for (int i = 0; i < Component.TYPE_IDS.size; i++) {
            int componentTypeId = Component.TYPE_IDS.get(i);
            entity.destroy(componentTypeId);
        }

        // remove the entity itself allowing it to be garbage collected
        entities.remove(entity.id);
    }

    /**
     * Lookup an entity by id
     * @param id the integer id of the requested entity
     * @return optional containing the entity instance if found, empty optional otherwise
     */
    public Optional<Entity> get(int id) {
        var entity = entities.get(id);
        if (entity == null) {
            Util.log(TAG, "Entity %d not found".formatted(id));
        }
        return Optional.ofNullable(entity);
    }

    /**
     * Remove <strong>all</strong> {@link Entity} instances and their attached {@link Component} instances
     */
    public void clear() {
        Util.log(TAG, "Destroying all entities and their attached components!");
        for (int i = entities.size - 1; i >= 0; i--) {
            var entity = entities.get(i);
            destroy(entity);
        }
        entities.clear();
    }

    // ------------------------------------------------------------------------
    // Component operations
    // ------------------------------------------------------------------------

    /**
     * Get all components of the specified type
     * @param componentTypeId the unique id of the {@link Component} type of the components to retrieve
     * @return non-null array containing all components of the given type, if any
     */
    @SuppressWarnings("unchecked")
    public <C extends Component> Array<C> getComponents(int componentTypeId) {
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
     * @param component the {@link Component} to add
     * @param componentTypeId the unique id of the {@link Component} type of the component to add
     */
    public void addComponent(Component component, int componentTypeId) {
        if (component == null) {
            Util.log(TAG, "add() called with null Component value");
            return;
        }

        var components = getComponents(componentTypeId);
        components.add(component);
    }

    /**
     * Remove the specified component from the global collection, if they're in it
     * @param component the {@link Component} to remove
     * @param componentTypeId the unique id of the component's concrete type
     */
    public void destroyComponent(Component component, int componentTypeId) {
        if (component == null) {
            Util.log(TAG, "removeComponent(): component null, ignoring");
            return;
        }

        var entity = component.entity;
        if (entity != Entity.NONE) {
            Util.log(TAG, "removeComponent(): component attached to entity %d, detaching".formatted(entity.id));
            entity.detach(componentTypeId);
        }

        var components = getComponents(componentTypeId);
        components.removeValue(component, true);
    }
}
