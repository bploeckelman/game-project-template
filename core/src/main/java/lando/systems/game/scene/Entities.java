package lando.systems.game.scene;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.game.scene.entities.SimpleEntity;
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
    private final ObjectMap<Class<? extends Component>, Array<? extends Component>> componentsMap = new ObjectMap<>();

    /**
     * Updates all components
     */
    public void update(float dt) {
        for (var entity : entities.values()) {
            if (!entity.active) continue;
            entity.update(dt);
        }

        for (var componentType : componentsMap.keys()) {
            var components = componentsMap.get(componentType);
            for (var component : components) {
                if (!component.active) continue;
                component.update(dt);
            }
        }
    }

    public SimpleEntity create() {
        return new SimpleEntity();
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
     * Lookup an entity by id, casting to the specified type if applicable
     * @param id the integer id of the requested entity
     * @param clazz the {@link Class} to cast the requested entity to, if applicable
     * @return optional containing the entity instance if found, empty optional if not found,
     * empty optional if not of the specified type and casting would throw a {@link ClassCastException}
     */
    public <EntityType extends Entity> Optional<EntityType> get(int id, Class<EntityType> clazz) {
        var entity = entities.get(id);
        if (entity == null) {
            Util.log(TAG, "Entity %d not found".formatted(id));
        } else if (!clazz.isInstance(entity)) {
            var actualClass = entity.getClass().getName();
            Util.log(TAG, "Entity %d found, but instance of %s not %s".formatted(id, actualClass, clazz));
            entity = null;
        }
        return Optional.ofNullable(clazz.cast(entity));
    }

    /**
     * Add the specified entity to the collection, if not already added
     * @param entity the {@link Entity} to add
     */
    public void add(Entity entity) {
        if (entities.get(entity.id) != null) {
            Util.log(TAG, "Entity %d already exists, skipping add".formatted(entity.id));
            return;
        }
        entities.put(entity.id, entity);
    }

    /**
     * Add one or more entities at once
     * @param entities varargs entities to be added
     */
    public void add(Entity... entities) {
        for (var entity : entities) {
            add(entity);
        }
    }

    /**
     * Remove the specified entity from the collection, if they're in it
     * @param entity the {@link Entity} to remove
     * @return true if successfully removed, false otherwise
     */
    public boolean remove(Entity entity) {
        if (entity == null) {
            Util.log(TAG, "remove called with null Entity value");
            return false;
        }
        return remove(entity.id);
    }

    /**
     * Remove the specified entity from the collection, if they're in it
     * @param id the unique id of the {@link Entity} to remove
     * @return true if successfully removed, false otherwise
     */
    public boolean remove(int id) {
        if (!entities.containsKey(id)) {
            Util.log(TAG, "Entity %d not found in collection, unable to remove".formatted(id));
            return false;
        }

        // remove any components of any type associated with this entity
        var entity = entities.get(id);
        for (var componentType : componentsMap.keys()) {
            removeAll(entity, componentType);
        }

        var removed = entities.remove(id);
        return (removed != null && removed.id == id);
    }

    /**
     * Remove all entities currently being tracked
     */
    public void removeAll() {
        Util.log(TAG, "Removing all entities");
        entities.clear();
    }

    /**
     * Get all components of the specified type
     * @param clazz the {@link Class} of the components to retrieve
     * @return non-null array containing all components of the given type, if any
     */
    @SuppressWarnings("unchecked")
    public <ComponentType extends Component>
    Array<ComponentType> get(Class<ComponentType> clazz) {
        Array<ComponentType> components;
        if (componentsMap.containsKey(clazz)) {
            components = (Array<ComponentType>) componentsMap.get(clazz);
        } else {
            components = new Array<>(clazz);
            componentsMap.put(clazz, components);
        }
        return components;
    }

    /**
     * Add a new component, keyed by type
     * @param component the component to add
     * @param clazz the {@link Class} of the new component, used as a lookup key for all components of the same type
     */
    public <ComponentType extends Component>
    void add(ComponentType component, Class<ComponentType> clazz) {
        if (component == null) {
            Util.log(TAG, "add called with null Component value");
            return;
        }

        var components = get(clazz);
        components.add(component);
    }

    /**
     * Remove the specified component from the collection, if they're in it
     * @param component the {@link Component} to remove
     * @param clazz the {@link Class} of the component to remove
     * @return true if successfully removed, false otherwise
     */
    public <ComponentType extends Component>
    boolean remove(ComponentType component, Class<ComponentType> clazz) {
        if (component == null) {
            Util.log(TAG, "remove called with null Component value");
            return false;
        }

        var components = get(clazz);
        return components.removeValue(clazz.cast(component), true);
    }

    /**
     * Remove all the components of the specified type from the specified entity, if any
     * @param entity the entity who's components are to be removed
     * @param clazz the {@link Class} of the components to remove
     */
    public <ComponentType extends Component>
    void removeAll(Entity entity, Class<ComponentType> clazz) {
        if (entity == null) {
            Util.log(TAG, "removeAll called with null Entity value");
            return;
        }

        var components = get(clazz);
        for (int i = components.size - 1; i >= 0; i--) {
            var component = components.get(i);
            if (component.entity.id == entity.id) {
                Util.log(TAG, "removing %s from parent entity".formatted(component));
                components.removeIndex(i);
            }
        }
    }
}
