package lando.systems.game.assets;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import lando.systems.game.utils.Util;

public class Patches extends AssetContainer<Patches.Type, NinePatch> {

    private static final String folder = "patch/";

    public enum Type implements AssetType<NinePatch> {
          PLAIN("plain", 2, 2, 2, 2)
        , PLAIN_DIM("plain-dim", 2, 2, 2, 2)
        , PLAIN_GRADIENT("plain-gradient", 2, 2, 2, 2)
        , ROUNDED("rounded", 10, 10, 10, 10)
        ;

        private final String regionName;
        private final int left;
        private final int right;
        private final int top;
        private final int bottom;

        Type(String regionName) {
            this(regionName, 1, 1, 1, 1);
            Util.log("Patches.Type", "*** Patches asset created without specifying cut edges, defaulting to 1 px edges");
        }

        Type(String regionName, int left, int right, int top, int bottom) {
            this.regionName = folder + regionName;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }

    public Patches() {
        super(Patches.class, NinePatch.class);
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
            var patch = new NinePatch(region, type.left, type.right, type.top, type.bottom);
            resources.put(type, patch);
        }
    }
}
