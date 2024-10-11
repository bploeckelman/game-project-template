package lando.systems.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.game.Config;
import lando.systems.game.utils.Util;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Assets implements Disposable {

    public enum Load { SYNC, ASYNC }
    public boolean loaded = false;

    public final ObjectMap<Class<? extends AssetContainer<?, ?>>, AssetContainer<?, ?>> containers;
    public final Preferences prefs;
    public final AssetManager mgr;
    public final SpriteBatch batch;
    public final ShapeDrawer shapes;
    public final GlyphLayout layout;
    public final Array<Disposable> disposables;

    public TextureAtlas atlas;
    public I18NBundle strings;

    public final Texture pixel;

    public TextureRegion pixelRegion;

    public Assets() {
        this(Load.SYNC);
    }

    public Assets(Load load) {
        prefs = Gdx.app.getPreferences(Config.preferences_name);

        containers = new ObjectMap<>();
        containers.put(Icons.class, new Icons());
        containers.put(Patches.class, new Patches());
        containers.put(ScreenTransitions.class, new ScreenTransitions());

        mgr = new AssetManager();
        batch = new SpriteBatch();
        shapes = new ShapeDrawer(batch);
        layout = new GlyphLayout();
        disposables = new Array<>();
        disposables.add(mgr);
        disposables.add(batch);

        // create a single pixel texture and associated region
        var pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        {
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            pixmap.drawPixel(1, 0);
            pixmap.drawPixel(0, 1);
            pixmap.drawPixel(1, 1);

            pixel = new Texture(pixmap);
            pixelRegion = new TextureRegion(pixel);
        }
        disposables.add(pixmap);
        disposables.add(pixel);

        // populate asset manager
        {
            // one-off items
            mgr.load("sprites/sprites.atlas", TextureAtlas.class);
            mgr.load("i18n/strings", I18NBundle.class);

            // textures
            mgr.load("images/libgdx.png", Texture.class);

            // fonts // TODO: asset loader for FreeTypeFont that takes ttf filename and params?

            // music

            // sounds

            // shaders // TODO: asset loader for ShaderProgram, or just use the util method?
        }

        if (load == Load.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (loaded) return 1;
        if (!mgr.update()) {
            return mgr.getProgress();
        }

        atlas = mgr.get("sprites/sprites.atlas");
        strings = mgr.get("i18n/strings");

        for (var container : containers.values()) {
            container.init(this);
        }

        loaded = true;
        return 1;
    }

    public <AssetContainerType extends AssetContainer<?, ?>>
    AssetContainerType get(Class<AssetContainerType> containerType) {
        if (containerType == null) {
            Util.log("Assets", "Unable to get AssetContainer, null container type class provided");
            return null;
        }

        var container = containers.get(containerType);
        if (container == null) {
            Util.log("Assets", "AssetContainer not found for type '%s'".formatted(containerType.getName()));
            return null;
        }
        return containerType.cast(container);
    }

    /**
     * Get resource from {@link AssetContainer} managing assets keyed by {@link AssetEnum} for a given {@link Resource} type
     * @param containerType {@link Class} instance corresponding to the {@link Container} type
     * @param assetType an {@link Enum<Asset>} value for the given {@link Asset} type, the key to lookup an associated {@link Resource} instance
     * @param <Asset> {@link AssetEnum} type
     * @param <Resource> {@link AssetEnum<Resource>} resource type
     * @param <Container> {@link AssetContainer} type for the given Asset/Resource types
     */
    public <Asset extends Enum<Asset> & AssetEnum<Resource>, Resource,
            Container extends AssetContainer<Asset, Resource>>
    Resource get(Class<Container> containerType, Asset assetType) {
        var container = get(containerType);
        if (container == null) {
            return null;
        }

        var asset = container.get(assetType);
        if (asset == null) {
            Util.log("Assets", "Asset(%s) not found in Container(%s)".formatted(assetType, containerType));
            return null;
        }
        return asset;
    }

    @Override
    public void dispose() {
        disposables.forEach(Disposable::dispose);
    }
}
