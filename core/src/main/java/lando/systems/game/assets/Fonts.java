package lando.systems.game.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import lando.systems.game.assets.framework.AssetContainer;
import lando.systems.game.assets.framework.AssetEnum;
import lando.systems.game.utils.Util;

import java.io.Serial;
import java.util.*;

/**
 * {@link AssetContainer} for {@link BitmapFont} instances.
 * <strong>NOTE: there are some mildly clever things going on in this container...</strong>
 * <ul>
 *     <li>
 *         each {@link Type} can specify multiple {@link Variant}s
 *         with different values for font size or other parameters,
 *         often for generating 'small', 'medium', and 'large' options
 *     </li>
 *     <li>
 *         the actual {@link BitmapFont} for a particular type/variant combo
 *         can be accessed through {@link Type#getFont}, if such a combo exists
 *     </li>
 *     <li>
 *         the {@link #load(Assets)} method is overridden so that the parameters
 *         specified in each {@link Variant} for each {@link Type} can be loaded
 *         by the {@link com.badlogic.gdx.assets.AssetManager} along with other assets
 *     </li>
 *     <li>
 *         while the asset manager usually uses the file path as a lookup key,
 *         but different variants of one {@link Type} reference the same font file
 *         so we must create a custom lookup key for each via {@link Type#assetMgrKey(Variant)}
 *     </li>
 *     <li>
 *         the generic {@code ResourceType} param is {@code Map<String, BitmapFont>},
 *         but since the constructor takes a {@code Class<ResourceType>} instance
 *         and Java erases generic type params, we use a 'named extension' of the
 *         concrete map type: {@link FontVariantMap} in order to be able to
 *         get a {@link Class} instance more specific than {@link Map}
 *     </li>
 * </ul>
 */
public class Fonts extends AssetContainer<Fonts.Type, Fonts.FontVariantMap> {

    private static final String TAG = Fonts.class.getSimpleName();

    /**
     * Extension of a parameterized collection type, a Java `typedef`.
     */
    public static class FontVariantMap extends HashMap<String, BitmapFont> {
        @Serial private static final long serialVersionUID = -7338435250472898944L;
    }

    public enum Type implements AssetEnum<FontVariantMap> {
          RISE       ("fonts/chevyray-rise.ttf")
        , ROUNDABOUT ("fonts/chevyray-roundabout.ttf",
            new Variant("small", 16),
            new Variant("medium", 32),
            new Variant("large", 64)
        )
        ;

        public final String fontFileName;
        public final List<Variant> variants;
        public final FontVariantMap fontByVariantName = new FontVariantMap();

        Type(String fontFileName) {
            this(fontFileName, new Variant());
        }

        Type(String fontFileName, Variant... variants) {
            this.fontFileName = fontFileName;

            // ensure we always have a 'default' variant
            var variantList = new ArrayList<>(Arrays.asList(variants));
            var noDefault = variantList.stream().noneMatch(v -> v.name.equals(Variant.DEFAULT_NAME));
            if (noDefault) {
                variantList.add(new Variant());
            }

            this.variants = Collections.unmodifiableList(variantList);
        }

        @Override
        public FontVariantMap resource() {
            return fontByVariantName;
        }

        public BitmapFont get() {
            return fontByVariantName.get(Variant.DEFAULT_NAME);
        }

        public BitmapFont getFont(String variantName) {
            var variant = fontByVariantName.get(variantName);
            return Optional.ofNullable(variant).orElseGet(() -> {
                Util.log(TAG, "failed to find font %s variant '%s', using 'default' instead"
                    .formatted(name(), variantName));
                return get();
            });
        }

        public String assetMgrKey(Variant variant) {
            var index = fontFileName.indexOf(".ttf");
            if (index == -1) {
                index = fontFileName.lastIndexOf(".otf");
            }
            // "fonts/foo{#variantName}.*tf"
            return "%s#%s%s".formatted(
                fontFileName.substring(0, index),
                variant.name,
                fontFileName.substring(index)
            );
        }

        public FreetypeFontLoader.FreeTypeFontLoaderParameter loaderParams(Variant variant) {
            var params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            params.fontFileName = fontFileName;
            params.fontParameters = variant.fontParameters;
            return params;
        }
    }

    public Fonts() {
        super(Fonts.class, FontVariantMap.class);
    }

    /**
     * Loads all the {@link BitmapFont} instances via the {@link com.badlogic.gdx.assets.AssetManager}
     */
    @Override
    public void load(Assets assets) {
        var mgr = assets.mgr;
        for (var type : Type.values()) {
            for (var variant : type.variants) {
                var key = type.assetMgrKey(variant);
                var loaderParams = type.loaderParams(variant);
                mgr.load(key, BitmapFont.class, loaderParams);
            }
        }
    }

    /**
     * Looks up the {@link BitmapFont} instances that were loaded in {@link #load}
     * into each {@link Type}'s {@link Type#fontByVariantName} map so they can
     * be looked up by {@link Type#getFont}
     */
    @Override
    public void init(Assets assets) {
        var mgr = assets.mgr;
        for (var type : Type.values()) {
            for (var variant : type.variants) {
                var key = type.assetMgrKey(variant);
                var font = mgr.get(key, BitmapFont.class);
                type.fontByVariantName.put(variant.name, font);
            }
        }
    }

    /**
     * Encapsulates some parameters from {@link FreeTypeFontGenerator.FreeTypeFontParameter}
     * used to define a variant of a given ttf font for a given {@link Type}.
     * There are more parameters available than those exposed in the constructors here,
     * but most are rarely used and we can add them if there's a need.
     */
    public static class Variant {

        public static final String DEFAULT_NAME = "default";
        public static final int DEFAULT_SIZE = 20;

        public final String name;
        public final FreeTypeFontGenerator.FreeTypeFontParameter fontParameters;

        public Variant() {
            this(DEFAULT_NAME, DEFAULT_SIZE);
        }

        public Variant(String variantName, int fontSize) {
            this(variantName,
                fontSize, Color.WHITE.cpy(),
                0, Color.WHITE.cpy(),
                0, 0, new Color(0, 0, 0, 0.75f),
                false, Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest
            );
        }

        public Variant(String variantName,
                       int fontSize, Color fontColor,
                       float borderWidth, Color borderColor,
                       int shadowOffsetX, int shadowOffsetY, Color shadowColor,
                       boolean genMipMaps, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {
            this.name = variantName;
            this.fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
            fontParameters.size = fontSize;
            fontParameters.color = fontColor;
            fontParameters.borderWidth = borderWidth;
            fontParameters.borderColor = borderColor;
            fontParameters.shadowOffsetX = shadowOffsetX;
            fontParameters.shadowOffsetY = shadowOffsetY;
            fontParameters.shadowColor = shadowColor;
            fontParameters.genMipMaps = genMipMaps;
            fontParameters.minFilter = minFilter;
            fontParameters.magFilter = magFilter;
        }
    }
}
