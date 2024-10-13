package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.game.Main;
import lando.systems.game.utils.Util;

public abstract class Component {

    private static final String TAG = Component.class.getSimpleName();

    // ========================================================================
    // NOTE: The following fields and methods are used to
    //  assign a unique id to each subclass of Component
    //  which is a Map key for components of that type,
    //  and also for assigning a Class<? extends Component> instance
    //  for ease of access when we need to cast to a concrete type.
    //  -
    //  The following must be in each Component subclass
    //  to ensure that the id and class get set once per type:
    //  ------------------------------------------------------
    //  public static final Integer type = Component.NEXT_TYPE_ID++;
    //  public static final Class<? extends Component> clazz = MyComponent.class;
    //  static {
    //      TYPE_IDS.add(type);
    //      TYPES.put(type, clazz);
    //  }
    //  public MyComponent() {
    //      super(type);
    //  }
    // ------------------------------------------------------------------------
    public static final IntArray TYPE_IDS = new IntArray();
    public static final IntMap<Class<? extends Component>> TYPES = new IntMap<>();

    public static final int INVALID_TYPE_ID = 0;
    public static final Class<? extends Component> INVALID_CLASS = InvalidComponent.class;

    protected static int NEXT_TYPE_ID = 1;
    // ========================================================================

    public Entity entity;
    public boolean active;

    public Component(int componentTypeId) {
        this.entity = Entity.NONE;
        this.active = true;
        Main.game.entities.addComponent(this, componentTypeId);
    }

    public void update(float dt) {}

    @Override
    public String toString() {
        return "%s(entity: %d)".formatted(getClass().getSimpleName(), entity.id);
    }
}
