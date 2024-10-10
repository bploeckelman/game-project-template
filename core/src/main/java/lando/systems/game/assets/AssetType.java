package lando.systems.game.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public interface AssetType<ResourceType> {

    class MethodImplementationMissingException extends GdxRuntimeException {
        private static final long serialVersionUID = -8967847562533119940L;

        public MethodImplementationMissingException(String methodName) {
            super("override %s to use param in concrete AssetType enum".formatted(methodName));
        }
    }

    default String textureRegionName() {
        throw new MethodImplementationMissingException("textureRegionName");
    }

    default float animFrameDuration() {
        throw new MethodImplementationMissingException("animFrameDuration");
    }

    default Array<String> animRegionNames() {
        throw new MethodImplementationMissingException("animRegionNames");
    }

    default Animation.PlayMode animPlayMode() {
        throw new MethodImplementationMissingException("animPlayMode");
    }
}
