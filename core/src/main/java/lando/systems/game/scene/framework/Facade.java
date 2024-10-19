package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.Array;

import java.util.Optional;
import java.util.stream.Stream;

public final class Facade {

    public interface Entities {
        Optional<Entity> get(int id);

        Entity create();

        void destroy(Entity entity);

        void clear();
    }

    public interface Components {
        Stream<Component> stream();

        <ComponentType extends Component> Array<ComponentType> getComponents(Class<ComponentType> clazz);

        <ComponentType extends Component> void add(Component component, Class<ComponentType> clazz);

        <ComponentType extends Component> void destroy(Component component, Class<ComponentType> clazz);
    }

    public interface Families {
        <FamilyType extends ComponentFamily> Array<FamilyType> getFamily(Class<FamilyType> clazz);
    }
}
