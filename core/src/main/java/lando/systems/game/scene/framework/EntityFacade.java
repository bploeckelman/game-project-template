package lando.systems.game.scene.framework;

import java.util.Optional;

public interface EntityFacade {
    Optional<Entity> get(int id);

    Entity create();

    void destroy(Entity entity);

    void clear();
}
