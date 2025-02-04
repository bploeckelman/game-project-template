package lando.systems.game.scene.framework;

import lando.systems.game.scene.Scene;
import lando.systems.game.screens.BaseScreen;

public abstract class Component {

    public Entity entity;
    public boolean active;

    /**
     * Create a {@link Component} instance, automatically attaching it to the specified {@link Entity}
     * and adding it to the specified {@link Entity}'s associated {@link World}
     */
    public Component(Entity entity) {
        this.active = true;
        var clazz = getClass();

        // attach this component to the specified Entity instance,
        // if a component of the same type wasn't already attached to it,
        // also sets the `Component::entity` field
        entity.attach(this, clazz);

        // add this component to the entity's World instance,
        // creating a container for the component type if not already created
        entity.scene.world.add(this, clazz);
    }

    /**
     * Create a standalone {@link Component} instance, not attached to any particular {@link Entity} instance.
     * <strong>should be used sparingly because having 'free floating' components is error prone</strong>
     */
    public Component(Scene<? extends BaseScreen> scene) {
        this.entity = Entity.NONE;
        this.active = true;
        scene.world.add(this, getClass());
    }

    /**
     * Convenience method for stream operations
     */
    public boolean active() {
        return active;
    }

    /**
     * Convenience method for stream operations
     */
    public boolean inactive() {
        return !active;
    }

    public void update(float dt) {
        // no-op by default
    }

    @Override
    public String toString() {
        return "%s(entity: %d)".formatted(getClass().getSimpleName(), entity.id);
    }
}
