package lando.systems.game.scene.framework;

public abstract class Component {

    public static final Component NONE = new Component() {
        // marker for component instances that are in an invalid state
    };

    public Entity entity;
    public boolean active;

    public Component() {
        this.entity = Entity.NONE;
        this.active = true;
        World.components.add(this, getClass());
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
    public boolean notActive() {
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
