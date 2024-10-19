package lando.systems.game.scene.framework;

public class ComponentInvalid extends Component {
    public static final Integer type = INVALID_TYPE_ID;
    public static final Class<? extends Component> clazz = ComponentInvalid.class;

    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public ComponentInvalid() {
        super(type);
    }
}
