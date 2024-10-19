package lando.systems.game.assets;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.game.assets.framework.AssetContainer;
import lando.systems.game.assets.framework.AssetEnum;
import lando.systems.game.utils.Util;

public class ScreenTransitions extends AssetContainer<ScreenTransitions.Type, ShaderProgram> {

    public static AssetContainer<ScreenTransitions.Type, ShaderProgram> container;

    public enum Type implements AssetEnum<ShaderProgram> {
        BLINDS,
        CIRCLECROP,
        CROSSHATCH,
        CUBE,
        DISSOLVE,
        DOOMDRIP,
        DOORWAY,
        DREAMY,
        HEART,
        PIXELIZE,
        RADIAL,
        RIPPLE,
        SIMPLEZOOM,
        STEREO;

        public static Type random() {
            var index = MathUtils.random(Type.values().length - 1);
            return Type.values()[index];
        }
    }

    public ScreenTransitions() {
        super(ScreenTransitions.class, ShaderProgram.class);
        ScreenTransitions.container = this;
    }

    @Override
    public void init(Assets assets) {
        var prefix = "shaders/transitions/";
        var vertex = prefix + "default.vert";
        for (var type : ScreenTransitions.Type.values()) {
            var filename = type.name().toLowerCase() + ".frag";
            var fragment = prefix + filename;
            var shader = Util.loadShader(vertex, fragment);
            resources.put(type, shader);
        }
    }
}
