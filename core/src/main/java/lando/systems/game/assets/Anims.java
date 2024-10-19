package lando.systems.game.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.game.assets.framework.AssetContainer;
import lando.systems.game.assets.framework.AssetEnum;

/**
 * {@link AssetContainer} implementation that contains an {@link Animation} asset
 * NOTE(brian): tried using java.lang.reflect.ParameterizedType to encapsulate the Animation type param
 *  but it didn't seems like there was a way to use the type params to get a non-raw class Animation out
 *  so we're just using the raw type and suppressing the warning
 */
@SuppressWarnings("rawtypes")
public class Anims extends AssetContainer<Anims.Type, Animation> {

    public static AssetContainer<Anims.Type, Animation> container;

    private static class Path {
        private static final String HERO = "character/hero/";
    }

    public static class AnimData {
        private static final float DEFAULT_FRAME_DURATION = 0.1f;
        private static final Animation.PlayMode DEFAULT_PLAY_MODE = Animation.PlayMode.LOOP;

        public final float frameDuration;
        public final Animation.PlayMode playMode;

        public AnimData() {
            this(DEFAULT_FRAME_DURATION, DEFAULT_PLAY_MODE);
        }

        public AnimData(float frameDuration) {
            this(frameDuration, DEFAULT_PLAY_MODE);
        }

        public AnimData(float frameDuration, Animation.PlayMode playMode) {
            if (frameDuration <= 0) {
                frameDuration = DEFAULT_FRAME_DURATION;
            }
            if (playMode == null) {
                playMode = DEFAULT_PLAY_MODE;
            }
            this.frameDuration = frameDuration;
            this.playMode = playMode;
        }
    }

    public enum Type implements AssetEnum<Animation> {
        // hero animations ------------------------------------------
        HERO_ATTACK_EFFECT(Path.HERO),
        HERO_ATTACK(Path.HERO),
        HERO_DEATH(Path.HERO),
        HERO_FALL(Path.HERO),
        HERO_IDLE(Path.HERO),
        HERO_JUMP(Path.HERO),
        HERO_LAND_EFFECT(Path.HERO),
        HERO_RUN(Path.HERO)
        // ----------------------------------------------------------
        ;

        private final String path;
        private final String name;
        private final AnimData data;

        Type(String path) {
            this(path, null, null);
        }

        Type(String path, AnimData data) {
            this(path, null, data);
        }

        Type(String path, String name) {
            this(path, name, null);
        }

        Type(String path, String name, AnimData data) {
            this.path = path;
            this.name = (name != null) ? name : name().toLowerCase().replace("_", "-");
            this.data = (data != null) ? data : new AnimData();
        }
    }

    public Anims() {
        super(Anims.class, Animation.class);
        Anims.container = this;
    }

    @Override
    public void init(Assets assets) {
        // TODO(brian): does AssetContainer.initInternal get called without an explicit super.initInternal call?
        var atlas = assets.atlas;
        for (var type : Type.values()) {
            var data = type.data;
            var regions = atlas.findRegions(type.path + type.name);
            var anim = new Animation<TextureRegion>(data.frameDuration, regions, data.playMode);
            resources.put(type, anim);
        }
    }
}
