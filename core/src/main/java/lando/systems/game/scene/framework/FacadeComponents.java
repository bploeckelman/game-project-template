package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.Array;

public interface FacadeComponents {
    <C extends Component> Array<C> getAll(int typeId);

    void add(Component component, int typeId);

    void destroy(Component component, int typeId);
}
