package lando.systems.game.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.game.utils.Util;

public class Icons extends AssetContainer<Icons.Type, TextureRegion> {

    private static final String folder = "icon/";

    public enum Type implements AssetType<TextureRegion> {
          CIRCLE_CHECK("circle-check")
        , CIRCLE_X("circle-x")
        , HEART("heart")
        , HEART_BROKEN("heart-broken")
        ;

        private final String regionName;

        Type(String regionName) {
            this.regionName = folder + regionName;
        }

        @Override
        public String textureRegionName() {
            return regionName;
        }
    }

    public Icons() {
        super(Icons.class, TextureRegion.class);
    }

    @Override
    public void init(Assets assets) {
        var atlas = assets.atlas;
        for (var type : Type.values()) {
            var region = atlas.findRegion(type.regionName);
            if (region == null) {
                Util.log(containerClassName, "init(): atlas region '%s' not found for type '%s'".formatted(type.regionName, type.name()));
                continue;
            }
            resources.put(type, region);
        }
    }
}
