package lando.systems.game.assets.framework;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.Serial;

public interface AssetEnum<ResourceType> {

    class MethodImplementationMissingException extends GdxRuntimeException {
        @Serial
        private static final long serialVersionUID = -8967847562533119940L;

        public MethodImplementationMissingException(String methodName) {
            super("override %s to use param in concrete AssetType enum".formatted(methodName));
        }
    }

    default ResourceType resourceType() {
        throw new MethodImplementationMissingException("resourceType");
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
